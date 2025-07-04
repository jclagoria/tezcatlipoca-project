package com.random.data.adapter.inbound.rest;

import com.random.data.application.service.DataService;
import com.random.data.domain.exception.*;
import com.random.data.domain.port.RateLimiterPort;
import com.random.data.domain.port.SerializePort;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class DataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 100;
    private final DataService dataService;
    private final RateLimiterPort rateLimiterPort;
    private final Map<String, SerializePort> serializers;
    private final Counter errorCounter;

    @Inject
    public DataController(DataService dataService,
                          RateLimiterPort rateLimiterPort,
                          @Any Instance<SerializePort> serializerInstances,
                          @Metric(
                                  name = "data_service_generate_errors",
                                  absolute = true,
                                  description = "Number of errors in calls to DataService#generate"
                          )
                              Counter errorCounter
    ) {
        this.dataService = dataService;
        this.rateLimiterPort = rateLimiterPort;
        this.serializers = serializerInstances.stream()
                .collect(Collectors.toMap(
                        SerializePort::format,
                        Function.identity()
                ));
        this.errorCounter = errorCounter;
    }

    @GET
    @Path("/{type}")
    @Counted(
            name        = "data_service_get_requests",
            absolute    = true,
            description = "Total number of GET /api/{type} requests"
    )
    @Timed(
            name        = "data_service_get_latency",
            absolute    = true,
            description = "Latency of GET /api/{type} endpoint"
    )
    public Uni<Response> getData(
            @PathParam("type") String type,
            @QueryParam("locale") @DefaultValue("en_US") String locale,
            @QueryParam("count") @DefaultValue("1") int count,
            @QueryParam("format") @DefaultValue("json") String format
    ) {
        LOGGER.debug("getData called: type={}, locale={}, count={}, format={}", type, locale, count, format);

        if (count < MIN_COUNT || count > MAX_COUNT) {
            LOGGER.error("Invalid count {}: must be between {} and {}", count, MIN_COUNT, MAX_COUNT);
            throw new InvalidParameterException(
                    String.format("Count must be between %d and %d", MIN_COUNT, MAX_COUNT)
            );
        }

        String key = format.trim().toLowerCase(Locale.ROOT);
        SerializePort serializer = serializers.get(key);
        if (serializer == null) {
            LOGGER.warn("Unsupported format requested: {}", format);
            throw new UnsupportedFormatException("Unsupported format: " + format);
        }

        try {
            rateLimiterPort.consume("/api/" + type);
            LOGGER.debug("Rate limiter allowed request for type={}", type);
        } catch (RateLimitExceededException e) {
            LOGGER.error("Rate limit exceeded for type={}", type);
            return Uni.createFrom().item(
                    Response.status(Response.Status.TOO_MANY_REQUESTS)
                            .entity(Map.of("error", "Rate limit exceeded"))
                            .build()
            );
        }

        return generateData(type, locale, count, key, serializer)
                .onFailure().invoke(e -> errorCounter.inc(1));
    }

    private Uni<Response> generateData(String type, String locale, int count, String key, SerializePort serializer) {
        return dataService.generate(type, locale, count)
                .onFailure().invoke(err ->
                        LOGGER.error("Error generating data", err)
                )
                .onFailure().transform(err -> {
                    if (err instanceof ApiException) {
                        return err;
                    }
                    return new DataControllerException(type, locale, count, err);
                })
                .flatMap(records ->
                        Uni.createFrom().item(() -> {
                                    try {
                                        return serializer.serialize(records);
                                    } catch (Exception e) {
                                        LOGGER.error("Error serializing data for type={}", e.getMessage());
                                        throw new DataSerializationException(key.toUpperCase(), e);
                                    }
                                })
                                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                )
                // now map payload → Response
                .map(payload -> {
                    String mediaType = "xml".equalsIgnoreCase(key)
                            ? MediaType.APPLICATION_XML
                            : MediaType.APPLICATION_JSON;
                    LOGGER.info("Responding with {}-encoded payload for type={}", key, type);
                    return Response.ok(payload, mediaType).build();
                });
    }

}

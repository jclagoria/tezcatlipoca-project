package com.random.data.adapter.inboud.rest;

import com.random.data.application.service.DataService;
import com.random.data.domain.port.RateLimiterPort;
import com.random.data.domain.port.SerializePort;
import com.random.data.domain.port.exception.RateLimitExceededException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Locale;
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

    @Inject
    public DataController(DataService dataService,
                          RateLimiterPort rateLimiterPort,
                          @Any Instance<SerializePort> serializerInstances) {
        this.dataService = dataService;
        this.rateLimiterPort = rateLimiterPort;
        this.serializers = serializerInstances.stream()
                .collect(Collectors.toMap(
                        SerializePort::format,    // returns "csv", "json", or "xml"
                        Function.identity()
                ));
    }

    @GET
    @Path("/{type}")
    public Uni<Response> getData(
            @PathParam("type") String type,
            @QueryParam("locale") @DefaultValue("en_US") String locale,
            @QueryParam("count") @DefaultValue("1") int count,
            @QueryParam("format") @DefaultValue("json") String format
    ) {
        LOGGER.debug("getData called: type={}, locale={}, count={}, format={}", type, locale, count, format);

        if (count < MIN_COUNT || count > MAX_COUNT) {
            LOGGER.warn("Invalid count {}: must be between {} and {}", count, MIN_COUNT, MAX_COUNT);
            throw new BadRequestException(
                    String.format("Count must be between %d and %d", MIN_COUNT, MAX_COUNT)
            );
        }

        // — normalize the requested format key —
        String key = format.trim().toLowerCase(Locale.ROOT);
        SerializePort serializer = serializers.get(key);
        if (serializer == null) {
            LOGGER.warn("Unsupported format requested: {}", format);
            throw new BadRequestException(
                    String.format("Format \"%s\" is not supported", format)
            );
        }

        try {
            rateLimiterPort.consume("/api/" + type);
            LOGGER.debug("Rate limiter allowed request for type={}", type);
        } catch (RateLimitExceededException e) {
            LOGGER.warn("Rate limit exceeded for type={}", type);
            return Uni.createFrom().item(
                    Response.status(Response.Status.TOO_MANY_REQUESTS)
                            .entity(Map.of("error", "Rate limit exceeded"))
                            .build()
            );
        }

        return dataService.generate(type, locale, count)
                .onItem().invoke(records ->
                        LOGGER.debug("Generated {} record(s) for type={}, locale={}", records.size(), type, locale)
                )
                .flatMap(records ->
                        Uni.createFrom().item(() -> serializer.serialize(records))
                                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                )
                .onItem().invoke(payload ->
                        LOGGER.debug("Serialization complete: {} bytes for format={}", payload.length(), format)
                )
                // Handle any failure in the reactive pipeline
                .onFailure().invoke(err ->
                        LOGGER.error("Error in processing getData for type=" + type, err)
                )
                .map(payload -> {
                    String mediaType = "xml".equalsIgnoreCase(format)
                            ? MediaType.APPLICATION_XML
                            : MediaType.APPLICATION_JSON;
                    LOGGER.info("Responding with {}-encoded payload for type={}", format, type);
                    return Response.ok(payload, mediaType).build();
                });
    }

}

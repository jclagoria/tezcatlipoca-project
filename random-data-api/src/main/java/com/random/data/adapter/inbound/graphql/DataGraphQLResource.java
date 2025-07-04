package com.random.data.adapter.inbound.graphql;

import com.random.data.application.service.DataService;
import com.random.data.domain.exception.RateLimitExceededException;
import com.random.data.domain.model.person.Person;
import com.random.data.domain.port.RateLimiterPort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.util.List;

@GraphQLApi
@ApplicationScoped
public class DataGraphQLResource {

    private final DataService dataService;
    private final RateLimiterPort rateLimiterPort;
    private final Counter errorCounter;

    @Inject
    public DataGraphQLResource(DataService dataService,
                               RateLimiterPort rateLimiterPort,
                               Counter errorCounter) {
        this.dataService = dataService;
        this.rateLimiterPort = rateLimiterPort;
        this.errorCounter = errorCounter;
    }

    @Counted(
            name = "data_graphql_persons_requests",
            description = "How many times persons(...) was called"
    )
    @Timed(
            name = "data_graphql_persons_latency",
            description = "Latency of persons(...) resolver"
    )
    @Query("persons")
    public Uni<List<Person>> getPersons(
            @DefaultValue("en_US") String locale,
            @DefaultValue("1") int count) {
        return Uni.createFrom().deferred(() -> {
                    rateLimiterPort.consume("/graphql/persons");
                    return dataService.generate("person", locale, count);
                })
                .map(list -> (List<Person>) list)
                // 1) Specific mapping for rate-limits
                .onFailure(RateLimitExceededException.class)
                .transform(failure -> new GraphQLException("Rate limit exceeded", failure))
                // 2) Count every failure
                .onFailure().invoke(err -> errorCounter.inc())
                // 3) *** Catch-all: wrap any other exception in a GraphQLException ***
                .onFailure().transform(failure -> {
                    if (failure instanceof GraphQLException) {
                        return failure;   // preserve ones we already wrapped
                    }
                    // now clients will see failure.getMessage() instead of "System error"
                    return new GraphQLException(failure.getMessage(), failure);
                });
    }
}

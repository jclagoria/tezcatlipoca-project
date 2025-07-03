package com.random.data.application.service;

import com.random.data.application.registration.ProviderRegistry;
import com.random.data.domain.port.DataProvider;
import com.random.data.domain.exception.ApiException;
import com.random.data.domain.exception.DataGenerationException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.smallrye.mutiny.helpers.spies.Spy.onFailure;

@ApplicationScoped
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    private final ProviderRegistry providerRegistry;
    private final Counter errorCounter;

    @Inject
    public DataService(ProviderRegistry providerRegistry,
                       @Metric(name="data_service_generate_errors",
                               description = "Number of errors in DataService#generate",
                               absolute=true) Counter errorCounter) {
        this.providerRegistry = providerRegistry;
        this.errorCounter = errorCounter;
    }

    @Timed(
            name = "data_service_generate_latency",
            description = "Latency of DataService#generate"
    )
    @Counted(
            name = "data_service_generate_throughput",
            description = "Number of calls to DataService#generate"
    )
    public Uni<List<?>> generate(String type, String locale, int count) {
        return actualGenerate(type, locale, count)
                .onFailure().invoke(e -> errorCounter.inc(1));
    }

    private Uni<List<?>> actualGenerate(String type, String locale, int count) {
        DataProvider<?> dataProvider = providerRegistry.getProvider(type);
        Uni<?> raw = dataProvider
                .generate(locale, count)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());

        Uni<List<?>> typed = (Uni<List<?>>)(Uni<?>) raw;

        return typed.onFailure().transform(failure -> {
            LOGGER.error(
                    "Error in DataService.generate() for type='{}', locale='{}', count={}'",
                    type, locale, count,
                    failure
            );
            if (failure instanceof ApiException) {
                return failure;
            }
            return new DataGenerationException(type, locale, count, failure);
        });
    }
}

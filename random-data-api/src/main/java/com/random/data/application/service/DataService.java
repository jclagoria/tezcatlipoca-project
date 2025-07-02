package com.random.data.application.service;

import com.random.data.application.registration.ProviderRegistry;
import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.ApiException;
import com.random.data.domain.port.exception.DataGenerationException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);
    private final ProviderRegistry providerRegistry;

    @Inject
    public DataService(ProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    @SuppressWarnings("unchecked") // for the one cast below
    public Uni<List<?>> generate(String type, String locale, int count) {
        // 1) lookup the provider
        DataProvider<?> dataProvider = providerRegistry.getProvider(type);

        // 2) call it (returns Uni<List<T>> for some T), offload to virtual threads
        Uni<?> raw = dataProvider
                .generate(locale, count)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor());

        // 3) cast once to Uni<List<?>>
        Uni<List<?>> typed = (Uni<List<?>>)(Uni<?>) raw;

        // 4) attach your failure‐mapper
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

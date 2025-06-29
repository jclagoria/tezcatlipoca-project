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

    public Uni<List<?>> generate(String type, String locale, int count) {
        // Log invocation immediately
        LOGGER.debug("generate() called: type={}, locale={}, count={}", type, locale, count);

        return Uni.createFrom()
                // force the generic to List<?> so the returned Uni is Uni<List<?>>
                .<List<?>>item(() -> {
                    // Lookup provider
                    LOGGER.debug("Looking up DataProvider for type='{}'", type);
                    DataProvider<?> dataProvider = providerRegistry.getProvider(type);
                    LOGGER.debug("Using provider '{}' for type='{}'", dataProvider.getClass().getSimpleName(), type);

                    // Generate data
                    List<?> result = dataProvider.generate(locale, count);
                    LOGGER.debug("DataProvider.generate() returned {} record(s) for type='{}', locale='{}'",
                            result.size(), type, locale);
                    return result;
                })
                // Offload blocking or CPU-intensive work to the default executor (e.g., virtual threads)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                // Post-generation hook
                .onItem().invoke(records ->
                        LOGGER.debug("generate() pipeline completed successfully: {} record(s) for type='{}'",
                                records.size(), type)
                )
                .onFailure().invoke(err ->
                        LOGGER.error("Error in DataService.generate() for type='{}', locale='{}', count={}",
                                type, locale, count, err)
                ).onFailure().transform(err -> {
                    if (err instanceof ApiException) {
                        return err;
                    }
                    return new DataGenerationException(type, locale, count, err);
                });
    }
}

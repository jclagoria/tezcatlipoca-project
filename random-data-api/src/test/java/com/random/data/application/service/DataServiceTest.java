package com.random.data.application.service;

import com.random.data.domain.port.DataProvider;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.random.data.application.service.DataServiceFixtures.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("DataService Unit Tests")
class DataServiceTest {

    private DataService service;

    @BeforeEach
    void setUp() {
        // Happy‐path: registry returns a provider that yields SAMPLE_LIST
        service = new DataService(registryWithProvider(sampleStringProvider()));
    }

    @Test
    @DisplayName("generate() emits the list returned by the provider")
    void testGenerateEmitsProviderResult() {
        Uni<List<?>> uni = service.generate(TYPE, LOCALE, COUNT);

        // block for the result
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) uni.await().indefinitely();

        assertThat(result)
                .isNotNull()
                .containsExactlyElementsOf(SAMPLE_LIST);
    }

    @Test
    @DisplayName("generate() fails when ProviderRegistry.getProvider() throws")
    void testGenerateFailsOnRegistryError() {
        RuntimeException registryEx = new IllegalArgumentException("registry missing");
        service = new DataService(registryThrowing(registryEx));

        Throwable thrown = catchThrowable(() ->
                service.generate(TYPE, LOCALE, COUNT)
                        .await().indefinitely()
        );

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("registry missing");
    }

    @Test
    @DisplayName("generate() fails when DataProvider.generate() throws")
    void testGenerateFailsOnProviderError() {
        RuntimeException providerEx = new IllegalStateException("generation error");
        DataProvider<String> badProvider = (locale, count) -> { throw providerEx; };
        service = new DataService(registryWithProvider(badProvider));

        Throwable thrown = catchThrowable(() ->
                service.generate(TYPE, LOCALE, COUNT)
                        .await().indefinitely()
        );

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("generation error");
    }

}
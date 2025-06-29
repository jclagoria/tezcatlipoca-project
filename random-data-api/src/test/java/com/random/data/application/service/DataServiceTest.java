package com.random.data.application.service;

import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.DataGenerationException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.random.data.application.service.DataServiceFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
    @DisplayName("generate() transforms registry failures into DataGenerationException")
    void testGenerateFailsOnRegistryError() {
        RuntimeException registryEx = new IllegalArgumentException("registry missing");
        service = new DataService(registryThrowing(registryEx));

        assertThatThrownBy(() -> service.generate(TYPE, LOCALE, COUNT)
                .await().indefinitely())
                .isInstanceOf(DataGenerationException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        String.format(
                                "Failed to generate data for type='%s', locale='%s', count=%d",
                                TYPE, LOCALE, COUNT
                        )
                );
    }

    @Test
    @DisplayName("generate() transforms provider failures into DataGenerationException")
    void testGenerateFailsOnProviderError() {
        RuntimeException providerEx = new IllegalStateException("generation error");
        DataProvider<String> badProvider = (locale, count) -> { throw providerEx; };
        service = new DataService(registryWithProvider(badProvider));

        assertThatThrownBy(() -> service.generate(TYPE, LOCALE, COUNT)
                .await().indefinitely())
                .isInstanceOf(DataGenerationException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        String.format(
                                "Failed to generate data for type='%s', locale='%s', count=%d",
                                TYPE, LOCALE, COUNT
                        )
                );
    }

}
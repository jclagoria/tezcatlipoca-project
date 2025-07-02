package com.random.data.application.service;

import com.random.data.application.registration.ProviderRegistry;
import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.ApiException;
import com.random.data.domain.port.exception.DataGenerationException;
import com.random.data.domain.port.exception.ProviderNotFoundException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataService Unit Tests")
class DataServiceTest {

    @Mock
    ProviderRegistry registry;

    @Mock
    @SuppressWarnings("unchecked")
    DataProvider<String> provider;

    DataService service;

    @BeforeEach
    void setUp() {
        service = new DataService(registry);
        // default to virtual threads for consistency
        Infrastructure.setDefaultExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Test
    @DisplayName("Happy path: returns generated list correctly")
    void shouldReturnGeneratedList() {
        // Stub registry lookup
        doReturn(provider).when(registry).getProvider(TestFixtures.TYPE);
        // Stub provider generate
        doReturn(Uni.createFrom().item(TestFixtures.sampleList()))
                .when(provider)
                .generate(TestFixtures.LOCALE, TestFixtures.COUNT);

        List<?> result = service.generate(TestFixtures.TYPE, TestFixtures.LOCALE, TestFixtures.COUNT)
                .await().indefinitely();

        assertThat(result).isEqualTo(TestFixtures.sampleList());
    }

    @ParameterizedTest(name = "Failure {0} maps to {1}")
    @MethodSource("providerFailureScenarios")
    @DisplayName("Error mapping: provider failures map to expected exceptions")
    void shouldMapProviderFailures(Throwable failure, Class<? extends Throwable> expected) {
        doReturn(provider).when(registry).getProvider(TestFixtures.TYPE);
        doReturn(Uni.createFrom().failure(failure))
                .when(provider)
                .generate(TestFixtures.LOCALE, TestFixtures.COUNT);

        assertThatThrownBy(() ->
                service.generate(TestFixtures.TYPE, TestFixtures.LOCALE, TestFixtures.COUNT)
                        .await().indefinitely()
        ).isInstanceOf(expected);
    }

    static Stream<Arguments> providerFailureScenarios() {
        return Stream.of(
                Arguments.of(
                        new ApiException(Response.Status.INTERNAL_SERVER_ERROR, "api fail") { },
                        ApiException.class
                ),
                Arguments.of(
                        new RuntimeException("other fail"),
                        DataGenerationException.class
                )
        );
    }

    @Test
    @DisplayName("Missing provider: thrown when registry has no provider for type")
    void shouldThrowWhenProviderMissing() {
        when(registry.getProvider("bad"))
                .thenThrow(new ProviderNotFoundException("bad", Set.of("x","y")));

        assertThatThrownBy(() ->
                service.generate("bad", TestFixtures.LOCALE, TestFixtures.COUNT)
        ).isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    @DisplayName("Executor scheduling: uses default executor for subscription")
    void shouldScheduleOnDefaultExecutor() {
        AtomicInteger calls = new AtomicInteger();
        Executor custom = runnable -> {
            calls.incrementAndGet();
            Executors.newVirtualThreadPerTaskExecutor().execute(runnable);
        };
        Infrastructure.setDefaultExecutor(custom);

        // 1) Stub the registry to return your mock provider
        doReturn(provider)
                .when(registry)
                .getProvider(TestFixtures.TYPE);

        // 2) Stub the provider to emit a real item on the virtual‐thread executor
        doReturn(Uni.createFrom().item(TestFixtures.sampleList()))
                .when(provider)
                .generate(TestFixtures.LOCALE, TestFixtures.COUNT);

        // 3) Run the service pipeline
        service.generate(TestFixtures.TYPE, TestFixtures.LOCALE, TestFixtures.COUNT)
                .await().indefinitely();

        // 4) Assert that our custom executor was actually invoked
        assertThat(calls.get()).isGreaterThan(0);
    }

    /**
     * Centralized test fixtures for reuse.
     */
    static class TestFixtures {
        static final String TYPE = "sample";
        static final String LOCALE = "en_US";
        static final int COUNT = 5;

        static List<String> sampleList() {
            return List.of("A", "B", "C", "D", "E");
        }
    }

}
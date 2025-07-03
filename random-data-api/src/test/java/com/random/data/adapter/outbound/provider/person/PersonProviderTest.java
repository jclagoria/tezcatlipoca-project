package com.random.data.adapter.outbound.provider.person;

import com.random.data.adapter.outbound.provider.person.model.Person;
import com.random.data.domain.exception.InvalidParameterException;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("Test for PersonProvider")
class PersonProviderTest {

    private PersonProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PersonProvider();
        // Reset the default executor to a known state before each test
        Infrastructure.setDefaultExecutor(ForkJoinPool.commonPool());
    }

    /**
     * Happy-path: generates the expected number of Person records with all fields non-null or non-empty.
     */
    @Test
    @DisplayName("Generate persons: correct count and non-null fields")
    void shouldGenerateCorrectCountAndNonNullFields() {
        List<Person> result = provider.generate(TestFixtures.VALID_LOCALE, TestFixtures.VALID_COUNT)
                .await().indefinitely();

        assertThat(result).hasSize(TestFixtures.VALID_COUNT);
        for (Person p : result) {
            assertThat(p.gender()).isNotBlank();
            assertThat(p.name()).isNotNull();
            assertThat(p.location()).isNotNull();
            assertThat(p.email()).contains("@");
            assertThat(p.login()).isNotNull();
            assertThat(p.dob()).isNotNull();
            assertThat(p.registered()).isNotNull();
            assertThat(p.phone()).isNotBlank();
            assertThat(p.cell()).isNotBlank();
            assertThat(p.id()).isNotNull();
            assertThat(p.picture()).isNotNull();
            assertThat(p.nat()).isNotBlank();
        }
    }

    /**
     * Validation: count must be at least 1.
     */
    @Test
    @DisplayName("Validate count: throw if less than one")
    void shouldThrowWhenCountLessThanOne() {
        assertThatThrownBy(() ->
                provider.generate(TestFixtures.VALID_LOCALE, 0)
                        .await().indefinitely()
        )
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage("Count must be at least 1");
    }

    /**
     * Parameterized validation for null, empty, or blank locale strings.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Validate locale: throw when null, empty, or blank")
    void shouldThrowWhenLocaleInvalid(String locale) {
        assertThatThrownBy(() ->
                provider.generate(locale, 1)
                        .await().indefinitely()
        )
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage("Locale cannot be null or empty");
    }

    /**
     * Verifies that the reactive pipeline is scheduled on the configured default executor (e.g., virtual threads in production).
     */
    @Test
    @DisplayName("Executor scheduling: should use default executor for subscription")
    void shouldUseDefaultExecutorForSubscription() {
        AtomicInteger executeCalls = new AtomicInteger();

        Executor customExecutor = runnable -> {
            executeCalls.incrementAndGet();
            // Delegate to a virtual thread executor to simulate production environment
            Executors.newVirtualThreadPerTaskExecutor().execute(runnable);
        };

        Infrastructure.setDefaultExecutor(customExecutor);

        // Trigger generation to schedule tasks on the custom executor
        provider.generate(TestFixtures.VALID_LOCALE, TestFixtures.VALID_COUNT)
                .await().indefinitely();

        assertThat(executeCalls.get()).isGreaterThan(0);
    }

    /**
     * Centralized test fixtures for reuse.
     */
    static class TestFixtures {
        static final String VALID_LOCALE = "en_US";
        static final int VALID_COUNT = 3;

        private TestFixtures() { /* Utility class */ }
    }

}
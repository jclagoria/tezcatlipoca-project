package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import com.random.data.domain.exception.MissingProviderKeyException;
import com.random.data.domain.exception.ProviderNotFoundException;
import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("ProviderRegistry Unit Tests")
class ProviderRegistryTest {

    @Nested
    @DisplayName("Happy‐path registrations")
    class HappyPath {

        @Test
        @DisplayName("Single provider is registered and looked up by key")
        void singleProviderRegistration_shouldRegisterAndLookup() {
            Instance<DataProvider<?>> inst =
                    ProviderRegistryFixtures.instanceOf(Collections.singletonList(
                            new ProviderRegistryFixtures.PersonProvider()
                    ));
            ProviderRegistry registry = new ProviderRegistry(inst);

            DataProvider<?> provider = registry.getProvider("person");
            assertThat(provider).isInstanceOf(ProviderRegistryFixtures.PersonProvider.class);
        }

        @Test
        @DisplayName("Multiple providers are registered and each can be fetched")
        void multipleProvidersRegistration_shouldRegisterAll() {
            Instance<DataProvider<?>> inst = ProviderRegistryFixtures.instanceOf(Arrays.asList(
                    new ProviderRegistryFixtures.PersonProvider(),
                    new ProviderRegistryFixtures.CompanyProvider()
            ));
            ProviderRegistry registry = new ProviderRegistry(inst);

            DataProvider<?> p1 = registry.getProvider("person");
            DataProvider<?> p2 = registry.getProvider("company");
            assertThat(p1).isInstanceOf(ProviderRegistryFixtures.PersonProvider.class);
            assertThat(p2).isInstanceOf(ProviderRegistryFixtures.CompanyProvider.class);
        }
    }

    @Nested
    @DisplayName("Lookup normalization")
    class Normalization {

        @ParameterizedTest(name = "[{index}] key=''{0}'' → class={1}")
        @CsvSource({
                "person, PersonProvider",
                " PERSON , PersonProvider",
                "company, CompanyProvider"
        })
        void lookup_validKeys_shouldReturnCorrectProvider(String rawKey, String expectedClassName) {
            Instance<DataProvider<?>> inst = ProviderRegistryFixtures.instanceOf(Arrays.asList(
                    new ProviderRegistryFixtures.PersonProvider(),
                    new ProviderRegistryFixtures.CompanyProvider()
            ));
            ProviderRegistry registry = new ProviderRegistry(inst);

            DataProvider<?> provider = registry.getProvider(rawKey);
            assertThat(provider.getClass().getSimpleName())
                    .isEqualTo(expectedClassName);
        }
    }

    @Nested
    @DisplayName("Error and edge cases")
    class Errors {

        @ParameterizedTest(name = "[{index}] invalid key=''{0}''")
        @ValueSource(strings = {"", "unknown", "   "})
        @DisplayName("lookup invalid or blank keys should throw ProviderNotFoundException")
        void lookup_invalidOrBlankKeys_shouldThrow(String key) {
            Instance<DataProvider<?>> inst =
                    ProviderRegistryFixtures.instanceOf(Collections.singletonList(
                            new ProviderRegistryFixtures.PersonProvider()
                    ));
            ProviderRegistry registry = new ProviderRegistry(inst);

            assertThatThrownBy(() -> registry.getProvider(key))
                    .isInstanceOf(ProviderNotFoundException.class)
                    .hasMessageContaining("No provider found for type");
        }

        @Test
        @DisplayName("constructor should throw MissingProviderKeyException when @ProviderKey is missing")
        void missingProviderKeyAnnotation_shouldThrowOnConstruction() {
            DataProvider<?> unkeyed = ProviderRegistryFixtures.unkeyedProvider();
            Instance<DataProvider<?>> inst =
                    ProviderRegistryFixtures.instanceOf(Collections.singletonList(unkeyed));

            assertThatThrownBy(() -> new ProviderRegistry(inst))
                    .isInstanceOf(MissingProviderKeyException.class)
                    .hasMessageContaining("is missing @ProviderKey");
        }

        @Test
        @DisplayName("getProvider(null) should throw ProviderNotFoundException")
        void getProvider_nullKey_shouldThrowNullPointer() {
            Instance<DataProvider<?>> inst =
                    ProviderRegistryFixtures.instanceOf(Collections.singletonList(
                            new ProviderRegistryFixtures.PersonProvider()
                    ));
            ProviderRegistry registry = new ProviderRegistry(inst);

            assertThatThrownBy(() -> registry.getProvider(null))
                    .isInstanceOf(ProviderNotFoundException.class)
                    .hasMessageContaining("No provider found for type");
        }
    }

}
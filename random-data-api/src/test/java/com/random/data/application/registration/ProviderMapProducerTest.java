package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderMapProducerTest {

    private final ProviderMapProducer producer = new ProviderMapProducer();

    @Nested
    @DisplayName("Normalization")
    class NormalizationTests {

        @Test
        @DisplayName("Should trim whitespace and lowercase the provider key")
        void shouldTrimAndLowercaseKey() {
            @ProviderKey("  Mixed Case  ")
            class P implements DataProvider<String> {
                @Override
                public List<String> generate(String type, int count) {
                    return List.of("value");
                }
            }

            Bean<DataProvider<?>> bean = TestFixtures.beanFor(P.class, new P());
            BeanManager bm = TestFixtures.beanManagerFor(Set.of(bean));
            producer.beanManager = bm;

            Map<String, DataProvider> map = producer.produceProviderMap();

            assertThat(map)
                    .containsKey("mixed case");
        }
    }

    @Nested
    @DisplayName("Happy-Path")
    class HappyPathTests {

        @Test
        @DisplayName("Should produce entries for distinct provider keys A and B")
        void shouldProduceMultipleDistinctKeys() {
            @ProviderKey("A")
            class AImpl implements DataProvider<String> {
                @Override
                public List<String> generate(String type, int count) {
                    return List.of("a");
                }
            }
            @ProviderKey("B")
            class BImpl implements DataProvider<String> {
                @Override
                public List<String> generate(String type, int count) {
                    return List.of("b");
                }
            }

            Bean<DataProvider<?>> beanA = TestFixtures.beanFor(AImpl.class, new AImpl());
            Bean<DataProvider<?>> beanB = TestFixtures.beanFor(BImpl.class, new BImpl());
            BeanManager bm = TestFixtures.beanManagerFor(Set.of(beanA, beanB));
            producer.beanManager = bm;

            Map<String, DataProvider> map = producer.produceProviderMap();

            assertThat(map)
                    .hasSize(2)
                    .containsKeys("a", "b");
        }
    }

    @Test
    @DisplayName("Missing @ProviderKey → IllegalStateException")
    void missingAnnotationShouldFail() {
        class NoKey implements DataProvider<String> {
            @Override
            public List<String> generate(String type, int count) {
                return List.of("x");
            }
        }
        Bean<DataProvider<?>> bean = TestFixtures.beanFor(NoKey.class, new NoKey());
        BeanManager bm = TestFixtures.beanManagerFor(Set.of(bean));
        producer.beanManager = bm;

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                producer::produceProviderMap
        );
        assertThat(ex.getMessage())
                .contains("No @ProviderKey on DataProvider implementation")
                .contains(NoKey.class.getName());
    }

    @Test
    @DisplayName("Duplicate normalized keys → IllegalStateException")
    void duplicateKeysShouldFail() {
        @ProviderKey("dup")
        class Impl1 implements DataProvider<String> {
            @Override
            public List<String> generate(String type, int count) {
                return List.of("1");
            }
        }
        @ProviderKey(" DUP ")
        class Impl2 implements DataProvider<String> {
            @Override
            public List<String> generate(String type, int count) {
                return List.of("2");
            }
        }

        Bean<DataProvider<?>> bean1 = TestFixtures.beanFor(Impl1.class, new Impl1());
        Bean<DataProvider<?>> bean2 = TestFixtures.beanFor(Impl2.class, new Impl2());
        BeanManager bm = TestFixtures.beanManagerFor(Set.of(bean1, bean2));
        producer.beanManager = bm;

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                producer::produceProviderMap
        );
        assertThat(ex.getMessage())
                .contains("Duplicate provider key 'dup'")
                .contains(Impl1.class.getName())
                .contains(Impl2.class.getName());
    }

}
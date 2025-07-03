package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import com.random.data.domain.exception.DuplicateProviderKeyException;
import com.random.data.domain.exception.MissingProviderKeyException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("ProviderMapProducer Tests")
class ProviderMapProducerTest {

    private final ProviderMapProducer producer = new ProviderMapProducer();

    @Nested
    @DisplayName("Provider key normalization rules")
    class ProviderKeyNormalizationTests {

        @Test
        @DisplayName("Trim whitespace and lowercase the provider key")
        void shouldTrimAndLowercaseKey() {
            @ProviderKey("  Mixed Case  ")
            class P implements DataProvider<String> {
                @Override
                public Uni<List<String>> generate(String type, int count) {
                    return (Uni<List<String>>) List.of("value");
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
    @DisplayName("Happy path: distinct provider keys")
    class HappyPathTests {

        @Test
        @DisplayName("Produce entries for distinct provider keys A and B")
        void shouldProduceMultipleDistinctKeys() {
            @ProviderKey("A")
            class AImpl implements DataProvider<String> {
                @Override
                public Uni<List<String>> generate(String type, int count) {
                    return (Uni<List<String>>) List.of("a");
                }
            }
            @ProviderKey("B")
            class BImpl implements DataProvider<String> {
                @Override
                public Uni<List<String>> generate(String type, int count) {
                    return (Uni<List<String>>) List.of("b");
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
    @DisplayName("Missing @ProviderKey should throw MissingProviderKeyException")
    void missingAnnotationShouldFail() {
        class NoKey implements DataProvider<String> {
            @Override
            public Uni<List<String>> generate(String type, int count) {
                return (Uni<List<String>>) List.of("x");
            }
        }
        Bean<DataProvider<?>> bean = TestFixtures.beanFor(NoKey.class, new NoKey());
        BeanManager bm = TestFixtures.beanManagerFor(Set.of(bean));
        producer.beanManager = bm;

        assertThatThrownBy(producer::produceProviderMap)
                .isInstanceOf(MissingProviderKeyException.class)
                .hasMessageContaining("missing @ProviderKey")
                .hasMessageContaining(NoKey.class.getName());
    }

    @Test
    @DisplayName("Duplicate normalized keys should throw DuplicateProviderKeyException")
    void duplicateKeysShouldFail() {
        @ProviderKey("dup")
        class Impl1 implements DataProvider<String> {
            @Override
            public Uni<List<String>> generate(String type, int count) {
                return (Uni<List<String>>)  List.of("1");
            }
        }
        @ProviderKey(" DUP ")
        class Impl2 implements DataProvider<String> {
            @Override
            public Uni<List<String>> generate(String type, int count) {
                return (Uni<List<String>>) List.of("2");
            }
        }

        Bean<DataProvider<?>> bean1 = TestFixtures.beanFor(Impl1.class, new Impl1());
        Bean<DataProvider<?>> bean2 = TestFixtures.beanFor(Impl2.class, new Impl2());
        BeanManager bm = TestFixtures.beanManagerFor(Set.of(bean1, bean2));
        producer.beanManager = bm;

        assertThatThrownBy(producer::produceProviderMap)
                .isInstanceOf(DuplicateProviderKeyException.class)
                .hasMessageContaining("Duplicate provider key 'dup'")
                .hasMessageContaining(Impl1.class.getName())
                .hasMessageContaining(Impl2.class.getName());
    }

}
package com.random.data.application.registration;

import com.random.data.adapter.outbound.serialiazer.CsvSerializer;
import com.random.data.domain.port.SerializePort;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("SerializerMapProducer Test")
class SerializerMapProducerTest {

    private BeanManager bm;
    private SerializerMapProducer producer;

    @BeforeEach
    void setup() {
        bm = mock(BeanManager.class);
        producer = new SerializerMapProducer();
        // inject our mock
        producer.beanManager = bm;
    }

    @Nested
    @DisplayName("Happy-path registration")
    class HappyPath {

        @Test
        @DisplayName("Single serializer registered")
        void singleSerializerRegistered() {
            CsvSerializer csv = new CsvSerializer();
            Bean<CsvSerializer> bean = SerializerTestFixtures.beanOf(bm, CsvSerializer.class, csv);
            when(bm.getBeans(SerializePort.class)).thenReturn(Set.of( bean));

            Map<String, SerializePort> map = producer.produceSerializerMap();
            assertThat(map)
                    .hasSize(1)
                    .containsEntry("csv", csv);
        }

        @Test
        @DisplayName("Multiple serializers registered")
        void multipleSerializersRegistered() {
            // create a mock Bean<SerializePort>
            @SuppressWarnings("unchecked")
            Bean<SerializePort> bean = mock(Bean.class);

            doReturn(CsvSerializer.class)
                    .when(bean)
                    .getBeanClass();

            CreationalContext<SerializePort> ctx = mock(CreationalContext.class);
            doReturn(ctx)
                    .when(bm)
                    .createCreationalContext(bean);

            // 3) stub the bean lookup to return exactly your one bean
            when(bm.getBeans(SerializePort.class))
                    .thenReturn(Set.of(bean));

            // 4) now have getReference(...) throw your test exception
            when(bm.getReference(bean, SerializePort.class, ctx))
                    .thenThrow(new RuntimeException("ref-fail"));

            // verify it actually bubbles up
            assertThatThrownBy(() -> producer.produceSerializerMap())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ref-fail");
        }

        @Test
        @DisplayName("Empty bean set returns empty map")
        void emptyBeanSet_returnsEmptyMap() {
            when(bm.getBeans(SerializePort.class)).thenReturn(Collections.emptySet());
            Map<String, SerializePort> map = producer.produceSerializerMap();
            assertThat(map).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error and edge cases")
    class Errors {

        @Test
        @DisplayName("Missing @SerializerKey throws exception")
        void missingAnnotationThrows() {
            // Stub a serializer without @SerializerKey
            class NoKeySerializer implements SerializePort {
                @Override public String serialize(java.util.List<?> records) { return ""; }
                @Override public String contentType() { return ""; }
                @Override public String format() { return ""; }
            }
            NoKeySerializer inst = new NoKeySerializer();
            Bean<NoKeySerializer> bean = SerializerTestFixtures.beanOf(bm, NoKeySerializer.class, inst);
            when(bm.getBeans(SerializePort.class)).thenReturn(Set.of(bean));

            assertThatThrownBy(() -> producer.produceSerializerMap())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No @SerializerKey");
        }

        @Test
        @DisplayName("Duplicate serializer keys throw exception")
        void duplicateCsvKeyThrows() {
            @SerializerKey("csv")
            class Stub1 implements SerializePort {
                @Override public String serialize(java.util.List<?> records) { return ""; }
                @Override public String contentType() { return ""; }
                @Override public String format() { return ""; }
            }
            @SerializerKey("csv")
            class Stub2 implements SerializePort {
                @Override public String serialize(java.util.List<?> records) { return ""; }
                @Override public String contentType() { return ""; }
                @Override public String format() { return ""; }
            }
            Stub1 s1 = new Stub1();
            Stub2 s2 = new Stub2();
            Bean<Stub1> b1 = SerializerTestFixtures.beanOf(bm, Stub1.class, s1);
            Bean<Stub2> b2 = SerializerTestFixtures.beanOf(bm, Stub2.class, s2);
            when(bm.getBeans(SerializePort.class)).thenReturn(Set.of(b1, b2));

            assertThatThrownBy(() -> producer.produceSerializerMap())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate serializer key: csv");
        }

        @Test
        @DisplayName("BeanManager.getReference exception propagates")
        void beanManagerReferenceException() {
            // make the bean match SerializePort rather than raw Bean<?>
            @SuppressWarnings("unchecked")
            Bean<SerializePort> bean = mock(Bean.class);

            // stub getBeanClass() with doReturn to avoid Class<CsvSerializer> ↛ Class<SerializePort> mismatch
            doReturn(CsvSerializer.class)
                    .when(bean)
                    .getBeanClass();

            // now stub createCreationalContext with the correct import
            CreationalContext<SerializePort> ctx = mock(CreationalContext.class);
            doReturn(ctx)
                    .when(bm)
                    .createCreationalContext(bean);

            // normal beans lookup
            when(bm.getBeans(SerializePort.class)).thenReturn(Set.of(bean));

            // finally stub getReference to throw
            when(bm.getReference(bean, SerializePort.class, ctx))
                    .thenThrow(new RuntimeException("ref-fail"));

            assertThatThrownBy(() -> producer.produceSerializerMap())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ref-fail");
        }
    }
}
package com.random.data.adapter.outbound.serialiazer;

import com.random.data.domain.port.exception.DataSerializationException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("XmlSerializer Tests")
class XmlSerializerTest {

    private XmlSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new XmlSerializer();
    }

    @Test
    @DisplayName("contentType returns application/xml; charset=UTF-8")
    void contentType_returnsApplicationXmlUtf8() {
        assertThat(serializer.contentType())
                .isEqualTo("application/xml; charset=UTF-8");
    }

    @Test
    @DisplayName("format returns xml")
    void format_returnsXml() {
        assertThat(serializer.format()).isEqualTo("xml");
    }

    @Test
    @DisplayName("serialize null returns empty string")
    void serialize_null_returnsEmptyString() {
        assertThat(serializer.serialize(null)).isEmpty();
    }

    @Test
    @DisplayName("serialize empty list renders empty wrapper")
    void serialize_emptyList_rendersEmptyWrapper() {
        String xml = serializer.serialize(TestFixtures.emptyXMLSimpleItemList());
        assertThat(xml).startsWith("<?xml");
        assertThat(xml).doesNotContain("<item>");
    }

    @Test
    @DisplayName("serialize simple item list includes all items")
    void serialize_simpleItemList_includesAllItems() {
        String xml = serializer.serialize(TestFixtures.simpleXMLItemList());
        // Should contain both item entries with names alpha and beta
        assertThat(xml).containsPattern("<item(\\s|>)");
        assertThat(xml).contains("<name>alpha</name>");
        assertThat(xml).contains("<name>beta</name>");
    }

    @Test
    @DisplayName("serialize when marshaller throws propagates DataSerializationException")
    void serialize_whenMarshallerThrows_propagatesRuntimeException() throws Exception {
        // prepare the mock JAXBContext and Marshaller
        JAXBContext mockCtx = mock(JAXBContext.class);
        Marshaller mockMarshaller = mock(Marshaller.class);
        when(mockCtx.createMarshaller()).thenReturn(mockMarshaller);
        doThrow(new JAXBException("fail"))
                .when(mockMarshaller).marshal(any(), any(Writer.class));

        // inject the mock context into the serializer
        Field contextsField = XmlSerializer.class.getDeclaredField("contexts");
        contextsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<Class<?>, JAXBContext> contexts =
                (ConcurrentMap<Class<?>, JAXBContext>) contextsField.get(serializer);
        contexts.put(TestFixtures.SimpleItem.class, mockCtx);

        // execute and assert DataSerializationException is thrown
        List<TestFixtures.SimpleItem> list = TestFixtures.simpleXMLItemList();
        assertThatThrownBy(() -> serializer.serialize(list))
                .isInstanceOf(DataSerializationException.class)
                .hasCauseInstanceOf(JAXBException.class)
                .hasRootCauseMessage("fail");
    }

}
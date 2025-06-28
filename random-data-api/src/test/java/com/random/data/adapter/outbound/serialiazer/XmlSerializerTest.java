package com.random.data.adapter.outbound.serialiazer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class XmlSerializerTest {

    private XmlSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new XmlSerializer();
    }

    @Test
    void contentType_returnsApplicationXmlUtf8() {
        assertThat(serializer.contentType())
                .isEqualTo("application/xml; charset=UTF-8");
    }

    @Test
    void format_returnsXml() {
        assertThat(serializer.format()).isEqualTo("xml");
    }

    @Test
    void serialize_null_returnsEmptyString() {
        assertThat(serializer.serialize(null)).isEmpty();
    }

    @Test
    void serialize_emptyList_rendersEmptyWrapper() {
        String xml = serializer.serialize(TestFixtures.emptyXMLSimpleItemList());
        // Should still produce XML declaration but no <item> elements
        assertThat(xml).startsWith("<?xml");
        assertThat(xml).doesNotContain("<item>");
    }

    @Test
    void serialize_simpleItemList_includesAllItems() {
        String xml = serializer.serialize(TestFixtures.simpleXMLItemList());
        // Should contain both item entries with names alpha and beta
        assertThat(xml).containsPattern("<item(\\s|>)");
        assertThat(xml).contains("<name>alpha</name>");
        assertThat(xml).contains("<name>beta</name>");
    }

    @Test
    void serialize_whenMarshallerThrows_propagatesRuntimeException() throws Exception {
        // preparamos el mock de JAXBContext y Marshaller
        JAXBContext mockCtx = mock(JAXBContext.class);
        Marshaller mockMarshaller = mock(Marshaller.class);
        when(mockCtx.createMarshaller()).thenReturn(mockMarshaller);
        // stubeamos: cuando se invoque marshal con cualquier objeto y cualquier Writer, lanza JAXBException
        doThrow(new JAXBException("fail"))
                .when(mockMarshaller).marshal(any(), any(Writer.class));

        // inyectamos el contexto en el serializer
        Field contextsField = XmlSerializer.class.getDeclaredField("contexts");
        contextsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<Class<?>,JAXBContext> contexts =
                (ConcurrentMap<Class<?>,JAXBContext>) contextsField.get(serializer);
        contexts.put(TestFixtures.SimpleItem.class, mockCtx);

        // ejecutamos y comprobamos que se propaga como RuntimeException
        List<TestFixtures.SimpleItem> list = TestFixtures.simpleXMLItemList();
        assertThatThrownBy(() -> serializer.serialize(list))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("XML serialization failed for")
                .hasRootCauseInstanceOf(JAXBException.class)
                .hasRootCauseMessage("fail");
    }

}
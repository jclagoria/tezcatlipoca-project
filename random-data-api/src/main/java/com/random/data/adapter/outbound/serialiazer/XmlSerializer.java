package com.random.data.adapter.outbound.serialiazer;

import com.random.data.application.registration.SerializerKey;
import com.random.data.domain.port.SerializePort;
import com.random.data.domain.port.exception.DataSerializationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
@SerializerKey("xml")
public class XmlSerializer implements SerializePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlSerializer.class);
    private final ConcurrentMap<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>();

    @Override
    public String serialize(List<?> records) {
        int size = records == null ? 0 : records.size();
        LOGGER.debug("XmlSerializer.serialize called with {} record(s)", size);

        if (records == null) {
            LOGGER.warn("No records provided for XML serialization—returning empty payload");
            return "";
        }

        // Determine element type (fallback to Object.class if empty)
        Class<?> elementType = records.isEmpty()
                ? Object.class
                : records.getFirst().getClass();
        LOGGER.debug("Determined JAXB element type: {}", elementType.getName());

        // Retrieve or create JAXBContext
        JAXBContext ctx = contexts.computeIfAbsent(elementType, cls -> {
            LOGGER.debug("Creating new JAXBContext for types: Wrapper and {}", cls.getName());
            return createJaxbContext(cls);
        });

        try {
            // Create and configure Marshaller
            Marshaller marshaller = ctx.createMarshaller();
            LOGGER.trace("JAXB Marshaller created for {}", elementType.getName());
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Wrap and marshal
            Wrapper<?> wrapper = new Wrapper<>(records);
            LOGGER.debug("Wrapped {} item(s) into JAXBElement", size);

            StringWriter writer = new StringWriter();
            marshaller.marshal(wrapper, writer);
            String xml = writer.toString();

            LOGGER.debug("XML serialization complete: {} characters", xml.length());
            return xml;

        } catch (JAXBException e) {
            LOGGER.error(
                    "JAXBException during XML serialization for type={} with {} record(s)",
                    elementType.getName(), size, e
            );
            throw new DataSerializationException("XML", e);
        }
    }

    @Override
    public String contentType() {
        String ct = MediaType.APPLICATION_XML + "; charset=UTF-8";
        LOGGER.debug("XmlSerializer.contentType() -> {}", ct);
        return ct;
    }

    @Override
    public String format() {
        return "xml";
    }

    /**
     * Lazily creates a JAXBContext for the given class and its associated Wrapper class.
     * This is necessary because JAXBContext creation is expensive and we don't want to
     * create it for every record type.
     *
     * @param cls the class to create the JAXBContext for
     * @return the JAXBContext for the given class
     */
    private static JAXBContext createJaxbContext(Class<?> cls) {
        try {
            LOGGER.trace("Initializing JAXBContext.newInstance for Wrapper and {}", cls.getName());
            return JAXBContext.newInstance(Wrapper.class, cls);
        } catch (JAXBException e) {
            LOGGER.error("Unable to initialize JAXBContext for {}", cls.getName(), e);
            throw new DataSerializationException("XML", e);
        }
    }
}

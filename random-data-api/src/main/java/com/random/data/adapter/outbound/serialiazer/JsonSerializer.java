package com.random.data.adapter.outbound.serialiazer;

import com.random.data.application.registration.ProviderKey;
import com.random.data.application.registration.SerializerKey;
import com.random.data.domain.port.SerializePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
@SerializerKey("json")
public class JsonSerializer implements SerializePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSerializer.class);
    private final Jsonb JSONB;

    public JsonSerializer() {
        this(JsonbBuilder.create());
    }

    /** package‐private constructor used by tests */
    JsonSerializer(Jsonb jsonb) {
        this.JSONB = jsonb;
    }

    @Override
    public String serialize(List<?> records) {
        int size = (records == null) ? 0 : records.size();
        LOGGER.debug("JsonSerializer.serialize called with {} record(s)", size);

        try {
            String json = JSONB.toJson(records);
            LOGGER.debug("JsonSerializer completed serialization: {} characters", json.length());
            return json;
        } catch (Exception e) {
            LOGGER.error("Error serializing {} record(s) to JSON", size, e);
            throw e;
        }
    }

    @Override
    public String contentType() {
        String ct = MediaType.APPLICATION_JSON + "; charset=UTF-8";
        LOGGER.debug("JsonSerializer.contentType() -> {}", ct);
        return ct;
    }

    @Override
    public String format() {
        LOGGER.debug("JsonSerializer.format() -> 'json'");
        return "json";
    }
}

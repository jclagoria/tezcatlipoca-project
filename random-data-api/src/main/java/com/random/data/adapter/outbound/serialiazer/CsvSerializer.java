package com.random.data.adapter.outbound.serialiazer;

import com.random.data.application.registration.SerializerKey;
import com.random.data.domain.port.SerializePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@SerializerKey("csv")
public class CsvSerializer implements SerializePort {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvSerializer.class);

    @Override
    public String serialize(List<?> records) {
        int size = records == null ? 0 : records.size();
        LOGGER.debug("CsvSerializer.serialize called with {} record(s)", size);

        if (records == null || records.isEmpty()) {
            LOGGER.info("No records to serialize: returning empty CSV payload");
            return "";
        }

        try {
            String csv = records.stream()
                    .map(Object::toString)
                    .map(this::escapeCsv)
                    .collect(Collectors.joining("\n"));

            LOGGER.debug("CsvSerializer completed serialization: {} characters", csv.length());
            return csv;
        } catch (Exception e) {
            LOGGER.error("Error serializing {} record(s) to CSV", size, e);
            throw e;
        }
    }

    /** Escape according to RFC 4180: double quotes and wrap fields containing special chars. */
    private String escapeCsv(String field) {
        boolean needsQuoting = field.contains("\"")
                || field.contains(",")
                || field.contains("\n")
                || field.contains("\r");

        if (needsQuoting) {
            String escaped = field.replace("\"", "\"\"");
            String quoted = "\"" + escaped + "\"";
            LOGGER.trace("Escaping CSV field: original='{}', escaped='{}'", field, quoted);
            return quoted;
        }

        return field;
    }

    @Override
    public String contentType() {
        String ct = MediaType.valueOf("text/csv;charset=UTF-8").toString();
        LOGGER.debug("CsvSerializer.contentType() -> {}", ct);
        return ct;
    }

    @Override
    public String format() {
        LOGGER.debug("CsvSerializer.format() -> 'csv'");
        return "csv";
    }
}

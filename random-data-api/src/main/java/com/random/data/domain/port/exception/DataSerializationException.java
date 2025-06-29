package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class DataSerializationException extends ApiException {
    public DataSerializationException(String format, Throwable cause) {
        super(Response.Status.INTERNAL_SERVER_ERROR,
                "Failed to serialize data to " + format,
                cause);
    }
}

package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class DataGenerationException extends ApiException {
    public DataGenerationException(String type, String locale, int count, Throwable cause) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                String.format(
                        "Failed to generate data for type='%s', locale='%s', count=%d",
                        type, locale, count
                ),
                cause
        );
    }
}

package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class UnsupportedFormatException extends ApiException {
    private final String format;

    public UnsupportedFormatException(String format) {
        super(
                Response.Status.BAD_REQUEST,
                String.format("Format \"%s\" is not supported", format)
        );
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}

package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class UnsupportedFormatException extends ApiException {
    public UnsupportedFormatException(String format) {
        super(
                Response.Status.BAD_REQUEST,
                String.format("Format \"%s\" is not supported", format)
        );
    }
}

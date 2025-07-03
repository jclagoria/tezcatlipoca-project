package com.random.data.domain.exception;

import jakarta.ws.rs.core.Response;

public class DataControllerException extends ApiException {
    public DataControllerException(String type, String locale, int count, Throwable cause) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                String.format("Failed to process request for type='%s', locale='%s', count=%d",
                        type, locale, count),
                cause
        );
    }
}

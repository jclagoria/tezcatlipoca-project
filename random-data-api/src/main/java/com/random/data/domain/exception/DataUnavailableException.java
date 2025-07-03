package com.random.data.domain.exception;

import jakarta.ws.rs.core.Response;

public class DataUnavailableException extends ApiException {
    public DataUnavailableException(String message) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
}

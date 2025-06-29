package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public abstract class ApiException extends RuntimeException {

    private final Response.Status status;

    protected ApiException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    protected ApiException(Response.Status status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }
}

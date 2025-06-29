package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class RateLimitExceededException extends ApiException {

    public RateLimitExceededException(String message) {
        super(Response.Status.TOO_MANY_REQUESTS, message);
    }
}

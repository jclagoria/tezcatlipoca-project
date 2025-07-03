package com.random.data.domain.exception;

import jakarta.ws.rs.core.Response;

public class InvalidParameterException extends ApiException {
    public InvalidParameterException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }
}

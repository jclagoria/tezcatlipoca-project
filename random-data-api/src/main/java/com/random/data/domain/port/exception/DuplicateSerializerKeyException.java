package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class DuplicateSerializerKeyException extends ApiException {
    public DuplicateSerializerKeyException(String key) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                "Duplicate serializer key detected: '" + key + "'"
        );
    }
}

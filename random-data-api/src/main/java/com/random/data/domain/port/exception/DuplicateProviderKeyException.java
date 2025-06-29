package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class DuplicateProviderKeyException extends ApiException {
    public DuplicateProviderKeyException(String key, Class<?> existing, Class<?> replacement) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                String.format(
                        "Duplicate provider key '%s' for classes %s and %s",
                        key,
                        existing.getName(),
                        replacement.getName()
                )
        );
    }
}

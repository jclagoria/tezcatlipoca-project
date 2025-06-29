package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class MissingProviderKeyException extends ApiException {
    public MissingProviderKeyException(Class<?> implClass) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                "DataProvider implementation "
                        + implClass.getName()
                        + " is missing @ProviderKey"
        );
    }
}

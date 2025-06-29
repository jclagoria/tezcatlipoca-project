package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

import java.util.Set;

public class ProviderNotFoundException extends ApiException {
    public ProviderNotFoundException(String type, Set<String> supported) {
        super(
                Response.Status.NOT_FOUND,
                "No provider found for type: '" + type +
                        "'. Supported types are: " + String.join(", ", supported)
        );
    }
}

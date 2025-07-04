package com.random.data.adapter.exception;

import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ErrorDto(
    String errorId,
    int statusCode,
    String status,
    String message,
    Instant timestamp,
    String documentationUrl,
    Map<String, Object> context
) {
    public static ErrorDto of(
        String errorId,
        int statusCode,
        String message,
        Instant timestamp,
        String documentationUrl
    ) {
        return new ErrorDto(
            errorId,
            statusCode,
            Response.Status.fromStatusCode(statusCode).getReasonPhrase(),
            message,
            timestamp,
            documentationUrl,
            Map.of()
        );
    }

    public static ErrorDto of(
        String errorId,
        int statusCode,
        String message,
        Instant timestamp,
        String documentationUrl,
        Map<String, Object> context
    ) {
        return new ErrorDto(
            errorId,
            statusCode,
            Response.Status.fromStatusCode(statusCode).getReasonPhrase(),
            message,
            timestamp,
            documentationUrl,
            context
        );
    }

    public ErrorDto withContext(Map<String, Object> context) {
        return new ErrorDto(
            errorId,
            statusCode,
            status,
            message,
            timestamp,
            documentationUrl,
            this.context == null ? context : 
                Map.copyOf(
                    Stream.concat(
                        this.context.entrySet().stream(),
                        context.entrySet().stream()
                    ).collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (v1, v2) -> v2
                    ))
                )
        );
    }
}

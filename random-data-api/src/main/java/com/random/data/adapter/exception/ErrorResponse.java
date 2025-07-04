package com.random.data.adapter.exception;

import java.time.Instant;

public record ErrorResponse(String errorId, String message, Instant timestamp) {
}

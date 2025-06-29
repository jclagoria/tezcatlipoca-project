package com.random.data.adapter.inboud.rest;

import java.time.Instant;

public record ErrorResponse(String errorId, String message, Instant timestamp) {
}

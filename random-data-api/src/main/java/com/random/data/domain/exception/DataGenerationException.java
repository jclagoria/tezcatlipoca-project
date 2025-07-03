package com.random.data.domain.exception;

import jakarta.ws.rs.core.Response;

public class DataGenerationException extends ApiException {
    private final String type;
    private final String locale;
    private final int count;

    public DataGenerationException(String type, String locale, int count, Throwable cause) {
        super(
                Response.Status.INTERNAL_SERVER_ERROR,
                String.format(
                        "Failed to generate data for type='%s', locale='%s', count=%d",
                        type, locale, count
                ),
                cause
        );
        this.type = type;
        this.locale = locale;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public String getLocale() {
        return locale;
    }

    public int getCount() {
        return count;
    }
}

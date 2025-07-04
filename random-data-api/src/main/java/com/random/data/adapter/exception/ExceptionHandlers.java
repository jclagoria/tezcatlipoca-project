package com.random.data.adapter.exception;

import com.random.data.domain.exception.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ExceptionHandlers {

    private static final String DOCUMENTATION_URL = "https://docs.tezcatlipoca-project.com/errors";

    @ServerExceptionMapper(ApiException.class)
    public Response toResponse(ApiException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            ex.getStatus().getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        );
        return Response
            .status(ex.getStatus())
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(InvalidParameterException.class)
    public Response handleInvalidParameter(InvalidParameterException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.BAD_REQUEST.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        );
        return Response
            .status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(ProviderNotFoundException.class)
    public Response handleProviderNotFound(ProviderNotFoundException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.NOT_FOUND.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        ).withContext(Map.of(
            "supportedTypes", ex.getSupportedTypes()
        ));
        return Response
            .status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(UnsupportedFormatException.class)
    public Response handleUnsupportedFormat(UnsupportedFormatException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.BAD_REQUEST.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        ).withContext(Map.of(
            "requestedFormat", ex.getFormat()
        ));
        return Response
            .status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(DataGenerationException.class)
    public Response handleDataGeneration(DataGenerationException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        ).withContext(Map.of(
            "type", ex.getType(),
            "locale", ex.getLocale(),
            "count", ex.getCount()
        ));
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(DataSerializationException.class)
    public Response handleDataSerialization(DataSerializationException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        ).withContext(Map.of(
            "format", ex.getFormat()
        ));
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(RateLimitExceededException.class)
    public Response handleRateLimit(RateLimitExceededException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.TOO_MANY_REQUESTS.getStatusCode(),
            ex.getMessage(),
            Instant.now(),
            DOCUMENTATION_URL
        );
        return Response
            .status(Response.Status.TOO_MANY_REQUESTS)
            .header("Retry-After", "60") // 60 seconds
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper(DataUnavailableException.class)
    public Response handleDataUnavailable(DataUnavailableException ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
                errorId,
                ex.getStatus().getStatusCode(),
                ex.getMessage(),
                Instant.now(),
                DOCUMENTATION_URL
        );
        return Response
                .status(ex.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

    @ServerExceptionMapper
    public Response handleAny(Throwable ex) {
        String errorId = UUID.randomUUID().toString();
        ErrorDto error = ErrorDto.of(
            errorId,
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "An unexpected error occurred",
            Instant.now(),
            DOCUMENTATION_URL
        );
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
}

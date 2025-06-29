package com.random.data.adapter.inboud.rest;

import com.random.data.domain.port.exception.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class ExceptionHandlers {

    @ServerExceptionMapper(ApiException.class)
    public Response toResponse(ApiException ex) {
        String errorId = UUID.randomUUID().toString();

        ErrorResponse bodyResponse = new ErrorResponse(
                errorId,
                ex.getMessage(),
                Instant.now()
        );

        return Response
                .status(ex.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(bodyResponse).build();
    }

    @ServerExceptionMapper
    public Response handleAny(Throwable ex) {
        String errorId = UUID.randomUUID().toString();

        ErrorResponse body = new ErrorResponse(
                errorId,
                "An unexpected error occurred",
                Instant.now()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}

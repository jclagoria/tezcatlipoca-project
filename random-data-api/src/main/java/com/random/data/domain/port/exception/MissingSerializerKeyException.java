package com.random.data.domain.port.exception;

import jakarta.ws.rs.core.Response;

public class MissingSerializerKeyException extends ApiException {
  public MissingSerializerKeyException(Class<?> implClass) {
    super(
            Response.Status.INTERNAL_SERVER_ERROR,
            "Serializer implementation "
                    + implClass.getName()
                    + " is missing @SerializerKey"
    );
  }
}

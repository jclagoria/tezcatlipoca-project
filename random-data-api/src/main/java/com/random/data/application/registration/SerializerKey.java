package com.random.data.application.registration;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({ ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD,        // ← allow on fields
        ElementType.PARAMETER })  // ← allow on constructor/method params })
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializerKey {
    String value();
}

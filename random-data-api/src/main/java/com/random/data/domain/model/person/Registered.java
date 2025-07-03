package com.random.data.domain.model.person;

import java.time.LocalDate;

public record Registered(
        LocalDate date,
        int age
)
{ }

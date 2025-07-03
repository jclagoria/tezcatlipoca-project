package com.random.data.domain.model.person;

import java.time.LocalDate;

public record Dob(
        LocalDate date,
        int age
)
{ }

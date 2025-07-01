package com.random.data.adapter.outbound.provider.person.model;

import java.time.LocalDate;

public record Dob(
        LocalDate date,
        int age
)
{ }

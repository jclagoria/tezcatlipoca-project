package com.random.data.adapter.outbound.provider.person.model;

import java.time.LocalDate;

public record Registered(
        LocalDate date,
        int age
)
{ }

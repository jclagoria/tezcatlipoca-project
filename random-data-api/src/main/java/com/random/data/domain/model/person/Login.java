package com.random.data.domain.model.person;

public record Login(
        String uuid,
        String username,
        String password,
        String salt
)
{ }

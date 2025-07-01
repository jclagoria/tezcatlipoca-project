package com.random.data.adapter.outbound.provider.person.model;

public record Login(
        String uuid,
        String username,
        String password,
        String salt
)
{ }

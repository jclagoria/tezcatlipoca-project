package com.random.data.adapter.outbound.provider.person.model;

public record Location(
        int streetNumber,
        String streetName,
        String city,
        String state,
        String country,
        int postcode)
{ }

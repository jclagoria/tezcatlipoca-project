package com.random.data.domain.model.person;

public record Location(
        int streetNumber,
        String streetName,
        String city,
        String state,
        String country,
        int postcode)
{ }

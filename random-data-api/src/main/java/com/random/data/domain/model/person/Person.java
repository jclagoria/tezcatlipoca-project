package com.random.data.domain.model.person;

public record Person(
        String gender,
        Name name,
        Location location,
        String email,
        Login login,
        Dob dob,
        Registered registered,
        String phone,
        String cell,
        Id id,
        Picture picture,
        String nat
) {

}

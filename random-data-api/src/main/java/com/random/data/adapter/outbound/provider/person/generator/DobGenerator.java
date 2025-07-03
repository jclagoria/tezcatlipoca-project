package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;
import com.random.data.domain.model.person.Dob;

import java.time.LocalDate;
import java.time.Period;

public class DobGenerator implements FakerGenerator<Dob> {
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 90;

    public static Dob generate(Faker faker) {
        LocalDate dob = LocalDate.now().minusYears(faker.number().numberBetween(MIN_AGE, MAX_AGE));
        int dobAge = Period.between(dob, LocalDate.now()).getYears();
        return new Dob(dob, dobAge);
    }
}

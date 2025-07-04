package com.random.data.adapter.outbound.provider.person.generator;

import com.random.data.domain.model.person.Registered;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.Period;

public class RegistrationGenerator implements FakerGenerator<Registered> {
    private static final LocalDate MIN_REG_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAX_REG_DATE = LocalDate.of(2025, 1, 1);
    private static final int DAYS_BETWEEN_REG_DATES = (int) MIN_REG_DATE.until(MAX_REG_DATE).getDays();

    public static Registered generate(Faker faker) {
        LocalDate regDate = MIN_REG_DATE.plusDays(faker.number().numberBetween(0, DAYS_BETWEEN_REG_DATES));
        int regAge = Period.between(regDate, LocalDate.now()).getYears();
        return new Registered(regDate, regAge);
    }
}

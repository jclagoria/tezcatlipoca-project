package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;

public class CellGenerator implements FakerGenerator<String> {

    public static String generate(Faker faker) {
        return faker.phoneNumber().cellPhoneInternational();
    }
}

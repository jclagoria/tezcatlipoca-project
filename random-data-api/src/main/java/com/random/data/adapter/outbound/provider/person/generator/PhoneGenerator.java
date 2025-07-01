package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;

public class PhoneGenerator implements FakerGenerator<String> {
    public static String generate(Faker faker) {
        return faker.phoneNumber().phoneNumberInternational();
    }
}

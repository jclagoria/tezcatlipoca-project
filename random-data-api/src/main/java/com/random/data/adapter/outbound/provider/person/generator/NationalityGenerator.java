package com.random.data.adapter.outbound.provider.person.generator;

import com.random.data.adapter.outbound.provider.person.util.WeightedRandom;
import net.datafaker.Faker;

public class NationalityGenerator implements FakerGenerator<String> {
    private static final String[] NATIONALITIES = {"IE", "GB", "US", "CA", "AU", "NZ"};
    private static final int[] NATIONALITY_WEIGHTS = {30, 20, 15, 15, 10, 10};

    public static String generate(Faker faker) {
        return WeightedRandom.select(NATIONALITIES, NATIONALITY_WEIGHTS);
    }
}

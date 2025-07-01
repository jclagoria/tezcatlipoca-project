package com.random.data.adapter.outbound.provider.person.generator;

import com.random.data.adapter.outbound.provider.person.model.Id;
import com.random.data.adapter.outbound.provider.person.util.WeightedRandom;
import net.datafaker.Faker;

public class IdGenerator implements FakerGenerator<Id> {
    private static final String[] ID_TYPES = {"PPS", "NI", "SSN", "NINO"};
    private static final int[] ID_WEIGHTS = {30, 20, 20, 30};

    public static Id generate(Faker faker) {
        String idName = WeightedRandom.select(ID_TYPES, ID_WEIGHTS);
        String idValue = faker.idNumber().valid();
        return new Id(idName, idValue);
    }
}

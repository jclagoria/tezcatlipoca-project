package com.random.data.adapter.outbound.provider.person.generator;

import com.random.data.domain.model.person.Name;
import com.random.data.adapter.outbound.provider.person.util.WeightedRandom;
import net.datafaker.Faker;

public class NameGenerator implements FakerGenerator<Name> {
    private static final String[] TITLES = {"Mr", "Mrs", "Ms", "Dr"};
    private static final int[] TITLE_WEIGHTS = {30, 30, 20, 20};

    /**
     * Generates a name using static method.
     * @param faker The Faker instance to use
     * @return Generated Name object
     */
    public static Name generate(Faker faker) {
        String title = WeightedRandom.select(TITLES, TITLE_WEIGHTS);
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        return new Name(title, firstName, lastName);
    }
}

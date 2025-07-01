package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;
import com.random.data.adapter.outbound.provider.person.model.Location;

public class LocationGenerator implements FakerGenerator<Location> {
    private static final int MIN_STREET_NUMBER = 1;
    private static final int MAX_STREET_NUMBER = 9999;
    private static final int MIN_POSTCODE = 1;
    private static final int MAX_POSTCODE = 99999;

    public static Location generate(Faker faker) {
        int streetNumber = faker.number().numberBetween(MIN_STREET_NUMBER, MAX_STREET_NUMBER);
        String streetName = faker.address().streetName();
        String city = faker.address().city();
        String state = faker.address().state();
        String country = faker.address().country();
        int postcode = faker.number().numberBetween(MIN_POSTCODE, MAX_POSTCODE);
        return new Location(streetNumber, streetName, city, state, country, postcode);
    }
}

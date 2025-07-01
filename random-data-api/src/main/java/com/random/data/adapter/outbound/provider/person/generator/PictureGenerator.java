package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;
import com.random.data.adapter.outbound.provider.person.model.Picture;

public class PictureGenerator implements FakerGenerator<Picture> {
    public static Picture generate(Faker faker) {
        String large = faker.internet().image(400, 400);
        String medium = faker.internet().image(200, 200);
        String thumbnail = faker.internet().image(50, 50);
        return new Picture(large, medium, thumbnail);
    }
}

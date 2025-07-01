package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;
import com.random.data.adapter.outbound.provider.person.model.Login;

public class LoginGenerator implements FakerGenerator<Login> {
    private static final int MIN_PASSWORD_LENGTH = 0;
    private static final int MAX_PASSWORD_LENGTH = 12;

    public static Login generate(Faker faker) {
        String uuid = faker.internet().uuid();
        String username = faker.internet().username();
        String password = faker.internet().password(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
        String salt = faker.internet().uuid();
        return new Login(uuid, username, password, salt);
    }
}

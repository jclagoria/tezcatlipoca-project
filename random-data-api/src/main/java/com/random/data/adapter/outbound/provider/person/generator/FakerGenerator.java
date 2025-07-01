package com.random.data.adapter.outbound.provider.person.generator;

import net.datafaker.Faker;

/**
 * Interface for generating fake data using Faker.
 */
public interface FakerGenerator<T> {

    /**
     * Generates a value using a static method of this generator.
     * @param faker The Faker instance to use for generation
     * @return The generated value
     */
    static <T> T generate(Faker faker, Class<? extends FakerGenerator<T>> generatorClass) {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) generatorClass
                    .getDeclaredMethod("generateStatic", Faker.class).invoke(null, faker);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke static generate method", e);
        }
    }
}

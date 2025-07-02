package com.random.data.adapter.outbound.provider.person;

import com.random.data.adapter.outbound.provider.person.generator.*;
import com.random.data.adapter.outbound.provider.person.model.Person;
import com.random.data.application.registration.ProviderKey;
import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.InvalidParameterException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider for generating random person data.
 * Uses DataFaker for generating realistic test data.
 * Optimized for large data generation with streaming support.
 */
@ApplicationScoped
@ProviderKey("person")
public class PersonProvider implements DataProvider<Person> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonProvider.class);
    private static final int BUFFER_SIZE = 100;

    private static final ConcurrentMap<String, Faker> FAKER_CACHE = new ConcurrentHashMap<>();

    /**
     * Generates a list of random Person records.
     *
     * @param locale The locale for data generation (e.g., "en_US" or "en-US")
     * @param count  Number of records to generate (must be ≥ 1)
     * @return Uni emitting the List of generated Person objects
     */
    @Override
    public Uni<List<Person>> generate(String locale, int count) {
        // 1) Validate once at entry
        validateParameters(locale, count);

        LOGGER.info("Generating {} Person records for locale={}", count, locale);

        // 2) Get or create a cached Faker for this locale
        Faker faker = FAKER_CACHE.computeIfAbsent(locale, this::newFakerInstance);

        // 3) Build the reactive pipeline, scheduled on Quarkus’s virtual-thread executor
        return Multi.createFrom().range(0, count)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .onItem().transform(i -> createPerson(faker))
                .onItem().transform(person -> {
                    // Guard per-item debug logging
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Generated person record {}", person);
                    }
                    return person;
                })
                .onOverflow().buffer(BUFFER_SIZE)
                .collect().asList();
    }

    /**
     * Validates input parameters.
     *
     * @param locale The locale string
     * @param count  Number of records to generate
     * @throws InvalidParameterException if parameters are invalid
     */
    private void validateParameters(String locale, int count) {
        if (count < 1) {
            LOGGER.warn("Invalid count {}: must be at least 1", count);
            throw new InvalidParameterException("Count must be at least 1");
        }
        if (locale == null || locale.trim().isEmpty()) {
            LOGGER.warn("Invalid locale: {}", locale);
            throw new InvalidParameterException("Locale cannot be null or empty");
        }
    }

    /**
     * Parses the locale string robustly (accepts both "en_US" and "en-US"),
     * and instantiates a Faker for that locale.
     *
     * @param locale The locale string
     * @return Configured Faker instance
     * @throws InvalidParameterException if the locale format is invalid
     */
    private Faker newFakerInstance(String locale) {
        try {
            String tag = locale.replace('_', '-');
            Locale loc = Locale.forLanguageTag(tag);
            if (loc.getLanguage().isEmpty()) {
                throw new IllegalArgumentException("Empty language in tag");
            }
            return new Faker(loc);
        } catch (Exception e) {
            LOGGER.error("Invalid locale format '{}': {}", locale, e.getMessage(), e);
            throw new InvalidParameterException("Invalid locale: " + locale);
        }
    }

    /**
     * Creates a complete Person object with all required fields.
     *
     * @param faker Faker instance for data generation
     * @return Generated Person object
     */
    private Person createPerson(Faker faker) {
        return new Person(
                faker.gender().binaryTypes(),
                NameGenerator.generate(faker),
                LocationGenerator.generate(faker),
                faker.internet().emailAddress(),
                LoginGenerator.generate(faker),
                DobGenerator.generate(faker),
                RegistrationGenerator.generate(faker),
                PhoneGenerator.generate(faker),
                CellGenerator.generate(faker),
                IdGenerator.generate(faker),
                PictureGenerator.generate(faker),
                NationalityGenerator.generate(faker)
        );
    }
}

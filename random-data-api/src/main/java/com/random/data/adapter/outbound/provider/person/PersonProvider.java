package com.random.data.adapter.outbound.provider.person;

import com.random.data.adapter.outbound.provider.person.generator.*;
import com.random.data.adapter.outbound.provider.person.model.Person;
import com.random.data.application.registration.ProviderKey;
import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.InvalidParameterException;
import jakarta.enterprise.context.ApplicationScoped;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Provider for generating random person data.
 * Uses DataFaker for generating realistic test data.
 * Optimized for large data generation.
 */
@ApplicationScoped
@ProviderKey("person")
public class PersonProvider implements DataProvider<Person> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonProvider.class);
    private static final ForkJoinPool POOL = new ForkJoinPool();
    private static final int BATCH_SIZE = 1000;

    /**
     * Generates a list of random person records.
     * @param locale The locale for data generation (e.g., "en_US")
     * @param count Number of records to generate
     * @return List of generated Person objects
     */
    @Override
    public List<Person> generate(String locale, int count) {
        LOGGER.info("Generating {} Person records for locale={}", count, locale);
        validateParameters(locale, count);

        Faker faker = createFaker(locale);
        
        // Use ForkJoinPool for parallel processing with controlled batch sizes
        return POOL.invoke(new PersonBatchTask(faker, count));
    }

    /**
     * Validates input parameters.
     * @param locale The locale string
     * @param count Number of records to generate
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
     * Creates a new Faker instance with the specified locale.
     * @param locale The locale string (e.g., "en_US")
     * @return Initialized Faker instance
     * @throws InvalidParameterException if locale format is invalid
     */
    private Faker createFaker(String locale) {
        try {
            String[] parts = locale.split("_");
            return new Faker(Locale.of(parts[0], parts[1]));
        } catch (Exception e) {
            LOGGER.error("Failed to create Faker instance for locale {}: {}", locale, e.getMessage());
            throw new InvalidParameterException("Invalid locale format: " + locale);
        }
    }

    /**
     * Creates a complete Person object with all required fields.
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

    /**
     * Task for generating a batch of persons.
     */
    private class PersonBatchTask extends RecursiveTask<List<Person>> {
        private final Faker faker;
        private final int count;

        PersonBatchTask(Faker faker, int count) {
            this.faker = faker;
            this.count = count;
        }

        @Override
        protected List<Person> compute() {
            if (count <= BATCH_SIZE) {
                // For small batches, generate sequentially
                return generateBatch(count);
            }

            // Split into smaller tasks
            int split = count / 2;
            PersonBatchTask left = new PersonBatchTask(faker, split);
            PersonBatchTask right = new PersonBatchTask(faker, count - split);

            // Start right task asynchronously
            right.fork();
            
            // Compute left task and wait for right
            List<Person> leftResult = left.compute();
            List<Person> rightResult = right.join();

            // Combine results
            leftResult.addAll(rightResult);
            return leftResult;
        }

        private List<Person> generateBatch(int count) {
            List<Person> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                result.add(createPerson(faker));
            }
            return result;
        }
    }
}

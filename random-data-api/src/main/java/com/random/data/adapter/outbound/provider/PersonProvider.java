package com.random.data.adapter.outbound.provider;

import com.random.data.application.registration.ProviderKey;
import com.random.data.domain.port.DataProvider;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
@ProviderKey("person")
public class PersonProvider implements DataProvider<Person> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonProvider.class);

    @Override
    public List<Person> generate(String locale, int count) {
        LOGGER.debug("PersonProvider.generate called with locale={} and count={}", locale, count);

        if (count < 1) {
            LOGGER.warn("Invalid count {}: must be at least 1", count);
            throw new IllegalArgumentException("count must be at least 1");
        }

        LOGGER.info("Generated Person record(s) for locale={}", locale);
        return IntStream.range(0, count)
                .mapToObj(i -> new Person("name" + 1,
                        "email" + i + "@gmail.com",
                        locale))
                .collect(Collectors.toList());
    }

}

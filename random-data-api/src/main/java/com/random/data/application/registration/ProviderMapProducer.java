package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import com.random.data.domain.port.exception.DuplicateProviderKeyException;
import com.random.data.domain.port.exception.MissingProviderKeyException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProviderMapProducer {

    private static final ConcurrentMap<Class<?>, String>  KEY_CACHE = new ConcurrentHashMap<>();

    @Inject
    BeanManager beanManager;

    /**
     * Produces an application-scoped Map of DataProvider beans.
     *
     * Steps:
     *  1. Query BeanManager for all beans of type DataProvider.
     *  2. For each Bean<?>:
     *     a. Read the implementation class and its @ProviderKey.
     *     b. Normalize (trim + toLowerCase) and cache the key.
     *     c. Obtain the contextual DataProvider instance.
     *  3. Collect entries into a ConcurrentHashMap, throwing an exception
     *     if two providers share the same normalized key.
     *
     * @return a Map from normalized provider key → provider instance
     * @throws IllegalStateException if a provider lacks @ProviderKey or
     *                               if duplicate keys are detected
     */
    @Produces
    @ApplicationScoped
    public Map<String, DataProvider> produceProviderMap() {
        @SuppressWarnings("unchecked")
        Set<Bean<?>> beans = beanManager.getBeans(DataProvider.class);

        return beans.stream()
                .map(bean -> {
                    Class<?> implClass = bean.getBeanClass();

                    ProviderKey ann = implClass.getAnnotation(ProviderKey.class);
                    if (ann == null) {
                        throw new MissingProviderKeyException(implClass);
                    }

                    String key = KEY_CACHE.computeIfAbsent(
                            implClass,
                            cls -> ann.value().trim().toLowerCase(Locale.ROOT)
                    );

                    DataProvider<?> instance = (DataProvider<?>)
                            beanManager.getReference(
                                    bean,
                                    DataProvider.class,
                                    beanManager.createCreationalContext(bean)
                            );
                    return Map.entry(key, instance);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> {
                                String key = KEY_CACHE.get(existing.getClass());
                                throw new DuplicateProviderKeyException(
                                        key,
                                        existing.getClass(),
                                        replacement.getClass());
                        },
                        ConcurrentHashMap::new
                ));
    }

}

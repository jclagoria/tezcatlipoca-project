package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import com.random.data.domain.exception.MissingProviderKeyException;
import com.random.data.domain.exception.ProviderNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class ProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderRegistry.class);
    private final Map<String, DataProvider<?>> providers = new HashMap<>();

    @Inject
    public ProviderRegistry(@Any Instance<DataProvider<?>> providerInstances) {
        for (DataProvider<?> provider : providerInstances) {
            // unwrap the CDI proxy to find the real bean class
            Class<?> impl = provider.getClass();
            ProviderKey keyAnno = null;
            while (impl != null && impl != Object.class) {
                keyAnno = impl.getAnnotation(ProviderKey.class);
                if (keyAnno != null) {
                    break;
                }
                impl = impl.getSuperclass();
            }
            if (keyAnno == null) {
                throw new MissingProviderKeyException(provider.getClass());
            }

            String key = keyAnno.value().trim().toLowerCase();
            Class<?> providedType = resolveGenericType(provider);

            System.out.printf("Registering provider '%s' → %s (%s)%n",
                    key, impl.getSimpleName(), providedType.getSimpleName());

            providers.put(key, provider);
        }
    }

    public DataProvider<?> getProvider(String type) {
        String key = (type == null ? "" : type.trim().toLowerCase());
        DataProvider<?> dataProvider = providers.get(key);

        if (dataProvider == null) {
            LOGGER.warn(
                    "No provider found for type='{}'. Supported types are: {}",
                    type, providers.keySet()
            );
            throw new ProviderNotFoundException(type, providers.keySet());
        }

        LOGGER.debug(
                "Found provider '{}' for type='{}'",
                dataProvider.getClass().getSimpleName(), type
        );
        return dataProvider;
    }

    public Set<String> supportedTypes() {
        return providers.keySet();
    }

    /**
     * Inspect the implementation’s generic interface to pull out T.
     * (Works so long as your class directly implements DataProvider<T>.)
     */
    private Class<?> resolveGenericType(DataProvider<?> provider) {
        for (Type iface : provider.getClass().getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt
                    && pt.getRawType() == DataProvider.class) {
                Type actual = pt.getActualTypeArguments()[0];
                if (actual instanceof Class<?> cls) {
                    return cls;
                }
            }
        }
        // fallback if you can’t find it
        return Object.class;
    }

}

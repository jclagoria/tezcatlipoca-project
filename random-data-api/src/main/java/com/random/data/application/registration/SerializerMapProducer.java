package com.random.data.application.registration;

import com.random.data.domain.port.SerializePort;
import com.random.data.domain.exception.DuplicateSerializerKeyException;
import com.random.data.domain.exception.MissingSerializerKeyException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class SerializerMapProducer {

    private static final ConcurrentMap<Class<?>, String> SERIALIZER_CACHE = new ConcurrentHashMap<>();

    @Inject
    BeanManager beanManager;

    @Produces
    @ApplicationScoped
    public Map<String, SerializePort> produceSerializerMap() {

        // Fetch all SerializePort beans
        Set<Bean<?>> beans = beanManager.getBeans(SerializePort.class);

        // Build map manually to retain key context in error handling
        ConcurrentMap<String, SerializePort> map = new ConcurrentHashMap<>();
        for (Bean<?> bean : beans) {
            Class<?> implClass = bean.getBeanClass();
            SerializerKey ann = implClass.getAnnotation(SerializerKey.class);
            if (ann == null) {
                throw new MissingSerializerKeyException(implClass);
            }
            String key = SERIALIZER_CACHE.computeIfAbsent(
                    implClass,
                    cls -> ann.value().trim().toLowerCase());

            // Obtain the instance
            @SuppressWarnings("unchecked")
            SerializePort instance = (SerializePort) beanManager.getReference(
                    (Bean<SerializePort>) bean,
                    SerializePort.class,
                    beanManager.createCreationalContext(bean));

            // Check for duplicates by key, not by instance
            if (map.putIfAbsent(key, instance) != null) {
                throw new DuplicateSerializerKeyException(key);
            }
        }
        return map;
    }

}

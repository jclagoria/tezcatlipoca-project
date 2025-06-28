package com.random.data.application.registration;

import com.random.data.domain.port.SerializePort;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class SerializerTestFixtures {
    /**
     * Creates a mock Bean<T> whose getBeanClass() returns implClass and whose
     * BeanManager.getReference(...) returns the provided instance.
     */
    @SuppressWarnings("unchecked")
    public static <T extends SerializePort> Bean<T> beanOf(
            BeanManager bm,
            Class<T> implClass,
            T instance
    ) {
        Bean<T> bean = mock(Bean.class);
        doReturn(implClass).when(bean).getBeanClass();

        CreationalContext<T> ctx = mock(CreationalContext.class);
        when(bm.createCreationalContext(bean)).thenReturn(ctx);

        // Use explicit type witness so Mockito correctly infers the return type
        Mockito.when(bm.getReference(bean, SerializePort.class, ctx))
                .thenReturn(instance);
        return bean;
    }

}

package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import java.util.*;

import static org.mockito.Mockito.*;

public class TestFixtures {

    private static final Map<Bean<DataProvider<?>>, DataProvider<?>> INSTANCES = new HashMap<>();

    private TestFixtures() {
        // utility class
    }

    /**
     * Create a mocked Bean for the given implementation class and instance.
     */
    @SuppressWarnings("unchecked")
    static Bean<DataProvider<?>> beanFor(Class<?> implClass, DataProvider<?> instance) {
        Bean<DataProvider<?>> bean = (Bean<DataProvider<?>>) mock(Bean.class);
        doReturn(implClass).when(bean).getBeanClass();
        INSTANCES.put(bean, instance);
        return bean;
    }

    /**
     * Create a mocked BeanManager that returns the provided beans, and stubs createCreationalContext
     * and getReference to return a dummy DataProvider instance for each bean.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BeanManager beanManagerFor(Set<Bean<DataProvider<?>>> beans) {
        BeanManager bm = mock(BeanManager.class);
        when(bm.getBeans(DataProvider.class)).thenReturn((Set) beans);

        for (Bean<?> raw : beans) {
            @SuppressWarnings("unchecked")
            Bean<DataProvider<?>> bean = (Bean<DataProvider<?>>) raw;
            CreationalContext<DataProvider<?>> ctx = mock(CreationalContext.class);
            when(bm.createCreationalContext(bean)).thenReturn(ctx);

            // now return the real instance you recorded:
            DataProvider<?> inst = INSTANCES.get(bean);
            when(bm.getReference(bean, DataProvider.class, ctx)).thenReturn(inst);
        }
        return bm;
    }
}

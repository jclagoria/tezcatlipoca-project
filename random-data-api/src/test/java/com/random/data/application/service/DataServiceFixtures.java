package com.random.data.application.service;

import com.random.data.application.registration.ProviderRegistry;
import com.random.data.domain.port.DataProvider;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DataServiceFixtures {
    public static final String TYPE   = "myType";
    public static final String LOCALE = "en_US";
    public static final int    COUNT  = 3;

    /** A sample result list used across tests. */
    public static final List<String> SAMPLE_LIST = List.of("one", "two", "three");

    /** A provider whose generate(...) always returns SAMPLE_LIST (ignoring inputs). */
    public static DataProvider<String> sampleStringProvider() {
        return (locale, count) -> SAMPLE_LIST;
    }

    /**
     * Returns a ProviderRegistry mock that:
     *   - returns the given provider for getProvider(TYPE)
     *   - throws IllegalArgumentException for any other input
     */
    public static ProviderRegistry registryWithProvider(DataProvider<?> provider) {
        ProviderRegistry registry = mock(ProviderRegistry.class);
        doReturn(provider)
                .when(registry)
                .getProvider(eq(TYPE));

        doReturn(null)  // you can similarly doThrow for the other stub if you want
                .when(registry)
                .getProvider(argThat(t -> t == null || !t.equals(TYPE)));
        return registry;
    }

    /**
     * Returns a ProviderRegistry mock whose getProvider(...) always throws the given exception.
     */
    public static ProviderRegistry registryThrowing(RuntimeException ex) {
        ProviderRegistry registry = mock(ProviderRegistry.class);
        when(registry.getProvider(anyString())).thenThrow(ex);
        return registry;
    }
}

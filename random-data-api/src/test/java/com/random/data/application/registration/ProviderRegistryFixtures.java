package com.random.data.application.registration;

import com.random.data.domain.port.DataProvider;
import jakarta.enterprise.inject.Instance;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProviderRegistryFixtures {
    @ProviderKey("person")
    public static class PersonProvider implements DataProvider<String> {
        @Override
        public List<String> generate(String locale, int count) {
            return Collections.nCopies(count, "foo");
        }
    }

    @ProviderKey("company")
    public static class CompanyProvider implements DataProvider<Integer> {
        @Override
        public List<Integer> generate(String locale, int count) {
            return Collections.nCopies(count, 42);
        }
    }

    public static DataProvider<?> unkeyedProvider() {
        // anonymous class without @ProviderKey
        return new DataProvider<>() {
            @Override
            public List<Object> generate(String locale, int count) {
                return List.of();
            }
        };
    }

    /**
     * Build a Mockito‐mocked Instance<T> that iterates over the given providers.
     */
    @SuppressWarnings("unchecked")
    public static <T> Instance<T> instanceOf(Collection<T> providers) {
        Instance<T> inst = mock(Instance.class);
        // when iterator() is called, return providers.iterator()
        when(inst.iterator())
                .thenAnswer((Answer<Iterator<T>>) inv -> providers.iterator());
        return inst;
    }
}

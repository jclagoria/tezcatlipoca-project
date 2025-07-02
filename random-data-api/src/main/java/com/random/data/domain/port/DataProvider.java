package com.random.data.domain.port;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface DataProvider<T> {

    /**
     * Returns a list of the specified size containing elements of type T.
     * @param locale the locale to generate the data for
     * @param count the number of elements to generate
     * @return a list of elements of type T, of the size specified by the count parameter
     */
    Uni<List<T>> generate(String locale, int count);

}

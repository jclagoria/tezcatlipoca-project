package com.random.data.domain.port;

import java.util.List;

public interface SerializePort {

    /**
     * Serializes the given list of records into a string, using the specified format.
     * @param records the list of records to serialize
     * @return the serialized string
     */
    String serialize(List<?> records);

    /**
     * Returns the content type of the serialized data.
     * @return the content type of the serialized data
     */
    String contentType();

    /**
     * The format of the data. This is used to determine which format to use for serialization.
     * Format name (e.g. json, csv, xml).
     * @return the format of the data
     */
    String format();

}

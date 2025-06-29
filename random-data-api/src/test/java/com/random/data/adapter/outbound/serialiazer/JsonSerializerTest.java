package com.random.data.adapter.outbound.serialiazer;

import com.random.data.domain.port.exception.DataSerializationException;
import jakarta.json.bind.Jsonb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JsonSerializer Tests")
class JsonSerializerTest {

    private JsonSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonSerializer();
    }

    @ParameterizedTest(name = "serialize({0}) → “{1}”")
    @MethodSource("serializeCases")
    @DisplayName("serialize various inputs yields expected JSON")
    void serialize_variousInputs_yieldsExpectedJson(List<?> input, String expected) {
        String result = serializer.serialize(input);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> serializeCases() {
        return Stream.of(
                // null → JSON-B.toJson(null) == "null"
                Arguments.of(null, "null"),
                // empty list → []
                Arguments.of(TestFixtures.emptyList(), "[]"),
                // simple strings → ["foo","bar"]
                Arguments.of(TestFixtures.stringJSONList(), "[\"foo\",\"bar\"]"),
                // map with insertion order → [{"k1":"v1","k2":42}]
                Arguments.of(TestFixtures.mapJSONList(), "[{\"k1\":\"v1\",\"k2\":42}]")
        );
    }

    @Test
    @DisplayName("serialize when Jsonb throws propagates DataSerializationException")
    void serialize_whenJsonbThrows_propagatesException() throws Exception {
        // prepare a mock that will throw
        Jsonb mockJsonb = mock(Jsonb.class);
        when(mockJsonb.toJson(TestFixtures.stringJSONList()))
                .thenThrow(new IllegalStateException("boom"));

        // use the package-private ctor to inject it
        JsonSerializer serializer = new JsonSerializer(mockJsonb);

        assertThatThrownBy(() -> serializer.serialize(TestFixtures.stringJSONList()))
                .isInstanceOf(DataSerializationException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to serialize data to JSON");
    }

    @Test
    @DisplayName("contentType returns application/json; charset=UTF-8")
    void contentType_returnsApplicationJsonUtf8() {
        assertThat(serializer.contentType())
                .isEqualTo("application/json; charset=UTF-8");
    }

    @Test
    @DisplayName("format returns json")
    void format_returnsJson() {
        assertThat(serializer.format()).isEqualTo("json");
    }

}
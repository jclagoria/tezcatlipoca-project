package com.random.data.adapter.outbound.serialiazer;

import com.random.data.domain.port.exception.DataSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("CsvSerializer Tests")
class CsvSerializerTest {

    private CsvSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new CsvSerializer();
    }

    @Test
    @DisplayName("serialize null list returns empty string")
    void serialize_nullList_returnsEmptyString() {
        String result = serializer.serialize(null);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("serialize empty list returns empty string")
    void serialize_emptyList_returnsEmptyString() {
        String result = serializer.serialize(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("serialize simple strings joins with newline")
    void serialize_simpleStrings_joinsWithNewline() {
        List<String> input = TestFixtures.simpleCVSStrings();
        String csv = serializer.serialize(input);
        // "alice\nbob\ncarol"
        assertThat(csv).isEqualTo(
                String.join("\n", input)
        );
    }

    @ParameterizedTest(name = "escape “{0}” → “{1}”")
    @MethodSource("escapeCases")
    @DisplayName("serialize single special string applies correct escaping")
    void serialize_singleSpecialString_appliesCorrectEscaping(String raw, String expected) {
        String csv = serializer.serialize(List.of(raw));
        assertThat(csv).isEqualTo(expected);
    }

    @Test
    @DisplayName("serialize when toString throws propagates DataSerializationException")
    void serialize_whenToStringThrows_propagatesException() {
        Object bad = TestFixtures.badCVSRecord();
        assertThatThrownBy(() -> serializer.serialize(List.of(bad)))
                .isInstanceOf(DataSerializationException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessage("Failed to serialize data to CSV");
    }

    @Test
    @DisplayName("contentType returns text/csv;charset=UTF-8")
    void contentType_returnsTextCsvUtf8() {
        String ct = serializer.contentType();
        assertThat(ct).isEqualTo("text/csv;charset=UTF-8");
    }

    @Test
    @DisplayName("format returns csv")
    void format_returnsCsv() {
        String fmt = serializer.format();
        assertThat(fmt).isEqualTo("csv");
    }

    static Stream<Arguments> escapeCases() {
        return Stream.of(
                Arguments.of("plain",      "plain"),
                Arguments.of("has,comma",  "\"has,comma\""),
                Arguments.of("has\"quote", "\"has\"\"quote\""),
                Arguments.of("has\nnewline","\"has\nnewline\""),
                Arguments.of("has\rcarriage","\"has\rcarriage\"")
        );
    }

}
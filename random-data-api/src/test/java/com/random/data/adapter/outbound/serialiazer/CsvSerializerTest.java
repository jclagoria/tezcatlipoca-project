package com.random.data.adapter.outbound.serialiazer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CsvSerializerTest {

    private CsvSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new CsvSerializer();
    }

    @Test
    void serialize_nullList_returnsEmptyString() {
        String result = serializer.serialize(null);
        assertThat(result).isEmpty();
    }

    @Test
    void serialize_emptyList_returnsEmptyString() {
        String result = serializer.serialize(Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
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
    void serialize_singleSpecialString_appliesCorrectEscaping(String raw, String expected) {
        String csv = serializer.serialize(List.of(raw));
        assertThat(csv).isEqualTo(expected);
    }

    @Test
    void serialize_whenToStringThrows_propagatesException() {
        Object bad = TestFixtures.badCVSRecord();
        assertThatThrownBy(() -> serializer.serialize(List.of(bad)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oops!");
    }

    @Test
    void contentType_returnsTextCsvUtf8() {
        String ct = serializer.contentType();
        assertThat(ct).isEqualTo("text/csv;charset=UTF-8");
    }

    @Test
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
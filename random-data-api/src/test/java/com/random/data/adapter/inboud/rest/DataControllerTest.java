package com.random.data.adapter.inboud.rest;

import com.random.data.application.registration.SerializerKey;
import com.random.data.application.service.DataService;
import com.random.data.domain.port.RateLimiterPort;
import com.random.data.domain.port.SerializePort;
import com.random.data.domain.port.exception.RateLimitExceededException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class DataControllerTest {

    @InjectMock
    DataService dataService;

    @InjectMock
    RateLimiterPort rateLimiterPort;

    @InjectMock
    @SerializerKey("json")
    SerializePort jsonSerializer;

    @InjectMock
    @SerializerKey("xml")
    SerializePort xmlSerializer;

    @BeforeEach
    void setupSerializerFormats() {
        Mockito.when(jsonSerializer.format()).thenReturn("json");
        Mockito.when(xmlSerializer.format()).thenReturn("xml");
    }

    @Test
    @DisplayName("GET /api/person with defaults → JSON 200")
    void shouldReturnJson200() {
        // Arrange
        Mockito.doNothing().when(rateLimiterPort).consume("/api/person");
        Mockito.when(dataService.generate("person", "en_US", 1))
                .thenReturn(Uni.createFrom().item(DataControllerTestFixtures.sampleRecords()));
        Mockito.when(jsonSerializer.serialize(DataControllerTestFixtures.sampleRecords()))
                .thenReturn(DataControllerTestFixtures.sampleJson());

        // Act & Assert
        given()
                .queryParam("locale", "en_US")
                .queryParam("count", 1)
                .queryParam("format", "json")
                .when()
                .get("/api/person")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(equalTo(DataControllerTestFixtures.sampleJson()));
    }

    @Test
    @DisplayName("GET /api/person?format=xml with defaults → XML 200")
    void shouldReturnXml200() {
        Mockito.doNothing().when(rateLimiterPort).consume("/api/foo");
        Mockito.when(dataService.generate("foo", "en_US", 1))
                .thenReturn(Uni.createFrom().item(DataControllerTestFixtures.sampleRecords()));
        Mockito.when(xmlSerializer.serialize(DataControllerTestFixtures.sampleRecords()))
                .thenReturn(DataControllerTestFixtures.sampleXml());

        given()
                .queryParam("locale", "en_US")
                .queryParam("count", 1)
                .queryParam("format", "xml")
                .when()
                .get("/api/foo")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_XML)
                .body(equalTo(DataControllerTestFixtures.sampleXml()));
    }

    @Test
    @DisplayName("GET /api/person → 429 when rate limit exceeded")
    void shouldReturn429OnRateLimit() {
        Mockito.doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimiterPort).consume("/api/person");

        given()
                .queryParam("locale", "en_US")
                .queryParam("count", 1)
                .queryParam("format", "json")
                .when()
                .get("/api/person")
                .then()
                .statusCode(429);
    }

    @ParameterizedTest(name = "count={0} → 400")
    @CsvSource({"0", "101"})
    void shouldRejectInvalidCount(int badCount) {
        given()
                .queryParam("locale", "en_US")
                .queryParam("count", badCount)
                .queryParam("format", "json")
                .when()
                .get("/api/foo")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Unsupported format → 400")
    void shouldRejectUnsupportedFormat() {
        given()
                .queryParam("locale", "en_US")
                .queryParam("count", 1)
                .queryParam("format", "yaml")
                .when()
                .get("/api/foo")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Service failure → 500")
    void shouldReturn500OnServiceError() {
        Mockito.doNothing().when(rateLimiterPort).consume("/api/foo");
        Mockito.when(dataService.generate("foo", "en_US", 1))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("boom")));

        given()
                .queryParam("locale", "en_US")
                .queryParam("count", 1)
                .queryParam("format", "json")
                .when()
                .get("/api/foo")
                .then()
                .statusCode(500);
    }

}
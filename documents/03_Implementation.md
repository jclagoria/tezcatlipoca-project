# Implementation

## Project Initialization

```bash
mvn io.quarkus:quarkus-maven-plugin:create \
  -DprojectGroupId=com.example \
  -DprojectArtifactId=random-data-api \
  -DclassName="com.example.RestController" \
  -Dextensions="resteasy,jsonb,smallrye-graphql,quarkus-smallrye-openapi,smallrye-metrics,quarkus-redis-client"
```

## Define DataProvider Interface & Providers

Create src/main/java/com/example/api/DataProvider.java

Implement built-in providers: Person, Company, CreditCard, Lorem, Location, Animal, UUID.

## REST & GraphQL Controllers

@RestController.java: expose GET /api/{type}

GraphQLController.java: define schema and resolvers

## OpenAPI config in application.properties:

quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.path=/openapi

## Rate Limiting

Add Bucket4j and configure limits in RateLimiterFilter.java

application.yaml snippet:

rate-limiter:
  global: 10000
  per-ip: 1000

## Serialization & Format Support

Default JSON via Jackson

Implement CsvSerializer and XmlSerializer beans

## Metrics & Monitoring

Annotate with @Counted, @Timed

Enable /metrics endpoint

## Security Headers

Add security headers configuration in application.properties:

```properties
quarkus.http.headers.x-content-type-options=nosniff
quarkus.http.headers.x-frame-options=DENY
quarkus.http.headers.x-xss-protection=1; mode=block
quarkus.http.headers.strict-transport-security=max-age=31536000; includeSubDomains
```

## Error Handling

Create ErrorHandler.java:

```java
@ApplicationScoped
public class ErrorHandler {
    @Inject
    ObjectMapper objectMapper;

    public Response handleError(Exception e) {
        ErrorDto error = new ErrorDto();
        error.setCode(getErrorCode(e));
        error.setMessage(e.getMessage());
        if (e instanceof ValidationException) {
            error.setDetails(((ValidationException) e).getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.toList()));
        }
        return Response.status(getStatusCode(e))
                .entity(error)
                .build();
    }

    private String getErrorCode(Exception e) {
        if (e instanceof RateLimitExceededException) return "RATE_LIMIT_EXCEEDED";
        if (e instanceof ValidationException) return "INVALID_REQUEST";
        if (e instanceof LocaleNotSupportedException) return "INVALID_LOCALE";
        return "INTERNAL_ERROR";
    }

    private int getStatusCode(Exception e) {
        if (e instanceof RateLimitExceededException) return 429;
        if (e instanceof ValidationException) return 400;
        return 500;
    }
}
```

## Input Validation

Create DataValidator.java:

```java
@ApplicationScoped
public class DataValidator {
    @Inject
    Validator validator;

    public void validateRequest(String type, String locale, int count) {
        if (count <= 0 || count > 1000) {
            throw new ValidationException("Count must be between 1 and 1000");
        }
        if (!isValidLocale(locale)) {
            throw new LocaleNotSupportedException(locale);
        }
        if (!isValidType(type)) {
            throw new ValidationException("Invalid data type: " + type);
        }
    }

    private boolean isValidLocale(String locale) {
        // Validate against supported locales
        return Arrays.stream(Locale.getAvailableLocales())
            .map(Locale::toString)
            .anyMatch(locale::equals);
    }

    private boolean isValidType(String type) {
        // Validate against registered providers
        return ProviderRegistry.getInstance().hasProvider(type);
    }
}
```

## Docker & Kubernetes

Dockerfile:

```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime
COPY target/*-runner.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar"]
```
```bash
docker-compose.yaml and Helm charts for deployment
```

## CLI Tool

```java
@Command(name="randomgen", mixinStandardHelpOptions=true)
public class Cli implements Runnable {
  @Option(names="-t", description="Type") String type;
  @Option(names="-c", description="Count") int count = 1;
  @Option(names="-l", description="Locale") String locale = "en_US";
  public void run() {
    // HTTP request to localhost and print result
  }
}
```

## Testing

### Unit Tests

Create ProviderTest.java template:

```java
@QuarkusTest
public class ProviderTest {
    @Inject
    PersonProvider personProvider;

    @Test
    public void testGenerateValidPerson() {
        Person person = personProvider.generate(Locale.US);
        assertNotNull(person.getId());
        assertNotNull(person.getFirstName());
        assertNotNull(person.getLastName());
        // Add more assertions based on schema
    }

    @Test
    public void testLocaleSpecificData() {
        Person mexicanPerson = personProvider.generate(Locale.MX);
        assertTrue(isValidMexicanName(mexicanPerson.getFirstName()));
        assertTrue(isValidMexicanName(mexicanPerson.getLastName()));
    }
}
```

### Integration Tests

Create ApiTest.java:

```java
@QuarkusTest
public class ApiTest {
    @Test
    public void testPersonGeneration() {
        given()
            .queryParam("locale", "en_US")
            .queryParam("count", "3")
        .when()
            .get("/api/person")
        .then()
            .statusCode(200)
            .body("size()", equalTo(3))
            .body("[0].firstName", notNullValue());
    }

    @Test
    public void testRateLimiting() {
        // Test global rate limit
        // Test per-IP rate limit
    }
}
```

### Performance Tests

Create GatlingTest.scala:

```scala
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RandomDataLoadTest extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")

  val scn = scenario("Random Data Generation")
    .exec(
      http("Generate Person")
        .get("/api/person")
        .queryParam("locale", "en_US")
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsers(10000) during (10 seconds)
    ).protocols(httpProtocol)
  )
}
```

### Monitoring Tests

Create MonitoringTest.java:

```java
@QuarkusTest
public class MonitoringTest {
    @Test
    public void testMetrics() {
        // Test metrics collection
        // Test alert conditions
        // Test Grafana dashboard data
    }
}

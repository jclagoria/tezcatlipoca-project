# Method

To ensure modularity, maintainability, and testability, the service is structured following Hexagonal Architecture (Ports & Adapters) and adheres to SOLID design principles:

## Hexagonal Architecture: 
The core domain logic (data generation) lives at the center; REST and GraphQL controllers, format serializers, and rate limiter act as external adapters connected via well-defined ports (interfaces).

## SOLID Principles:

- Single Responsibility: Each provider, serializer, and controller has one clear responsibility.
- Open/Closed: New data providers or formats can be added via the DataProvider or FormatSerializer interfaces without modifying existing code.
- Liskov Substitution: Implementations of DataProvider<T> can be substituted transparently.
- Interface Segregation: Separate interfaces for data generation (DataProvider), serialization (FormatSerializer), and rate limiting (RateLimitService).
- Dependency Inversion: High-level modules (e.g., DataService) depend on abstractions (DataProvider<T>, FormatSerializer), not concrete implementations.

## Component Diagram

```plantuml
@startuml
package "API Layer" {
  RestController
  GraphQLController
}
package "Service Layer" {
  DataService
  ProviderRegistry
}
package "Providers" {
  PersonProvider
  CompanyProvider
  CreditCardProvider
  LoremProvider
  LocationProvider
  AnimalProvider
  UUIDProvider
}
package "Infrastructure" {
  RateLimiter
  MetricsCollector
}

RestController --> DataService : "GET /api/{type}"
GraphQLController --> DataService : "POST /graphql"
DataService --> ProviderRegistry : "resolve(type, locale)"
ProviderRegistry --> PersonProvider : "generate()"
ProviderRegistry --> CompanyProvider
ProviderRegistry --> CreditCardProvider
ProviderRegistry --> LoremProvider
ProviderRegistry --> LocationProvider
ProviderRegistry --> AnimalProvider
ProviderRegistry --> UUIDProvider
DataService --> RateLimiter : "check(request)"
DataService --> MetricsCollector : "record(request, latency)"
@enduml
```

## Data Provider Interface

```java
public interface DataProvider<T> {
  String getType();              // e.g., "person", "company", etc.
  T generate(Locale locale);     // generate one instance of type T
}
```

Built-in providers (e.g. PersonProvider, CompanyProvider) use libraries such as Java Faker under the hood, with locale passed through to produce region-specific data.

## Request Flow

- Client Request: HTTP GET /api/{type}?locale={locale}&count={n}&format={json|csv} or GraphQL POST with a query specifying type, count, and locale.
- Rate Limiting: Bucket4j enforces per-client and global limits using in-memory token buckets, configurable via application.yaml.
- Provider Resolution: DataService queries ProviderRegistry for the appropriate DataProvider.
- Data Generation: The provider’s generate() method is invoked n times to produce the requested records.
- Serialization: Records are serialized to JSON by default via Jackson; for CSV/XML, a pluggable FormatSerializer handles conversion.
- Response: The serialized payload is returned with HTTP 200. Errors return structured JSON error bodies.
- Metrics Recording: MetricsCollector exposes Prometheus-compatible metrics on /metrics.

## JSON Schema Example (Person)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Person",
  "type": "object",
  "properties": {
    "id": { "type":"string", "format":"uuid" },
    "firstName": { "type":"string" },
    "lastName": { "type":"string" },
    "email": { "type":"string", "format":"email" },
    "address": { "type":"string" }
  },
  "required": ["id","firstName","lastName","email"]
}
```

No persistent database is used; all data is generated in-memory per request, ensuring horizontal scalability and no data retention.

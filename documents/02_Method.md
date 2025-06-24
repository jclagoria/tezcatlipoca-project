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
- Custom data providers implement the `DataProvider<T>` interface and register automatically via CDI:
- Annotate each provider with `@ApplicationScoped` (and optionally `@Named` for explicit keys).  
- The `ProviderRegistry` (in `Infrastructure`) injects `Instance<DataProvider<?>>` and builds a lookup map at startup.  
- To add a new provider, simply place its implementation on the classpath—no core changes required.

## Concurrency & Threading Strategies

In order to sustain high throughput (e.g. 10 k RPS) while keeping event‐loop threads free, we adopt a hybrid model:
1. **Reactive, non‐blocking core**  
   - All endpoints return `Uni<T>` or `Multi<T>` (Mutiny) so Vert.x event‐loops never block.  
   - Use reactive clients (e.g. Reactive PgClient) and in‐memory generation logic that is cheap.
2. **Virtual Threads (Java 21+)**  
   - Offload providers or serializers that must block (file I/O, legacy libs, heavy JSON→CSV work) onto a bounded thread‐pool.
- Instead of managing a fixed worker-pool, you can leverage Project Loom’s virtual threads to isolate blocking calls without starving your event-loops:

```java
// create a virtual-thread per task executor
ExecutorService vtPool = Executors.newVirtualThreadPerTaskExecutor();

public Uni<List<CreditCard>> generate(int count) {
  return Uni.createFrom().item(() -> syncGenerateCreditCards(count))
            .runSubscriptionOn(vtPool);
}     
```

4. **Tunable pool sizes**  
   - You can simply note the JDK requirement; no extra properties are needed:
     ```yml
     # (Optional) When using virtual threads you do not need to tune worker-pool sizes,
      # but you must run on JDK 21 or later.

     ```
  - (If not using virtual threads) configure worker threads in `application.properties`:  
     ```yml
     quarkus.vertx.event-loops-pool-size=16
     quarkus.vertx.worker-pool-size=40
     ```

## Resiliency Patterns and Resilience4j Integration

As an alternative to MicroProfile Fault-Tolerance or Mutiny pipelines, we can use Resilience4j for a full suite of resiliency patterns:

1. **CircuitBreaker**  
   - Trips when failures exceed a threshold in a sliding window, short-circuiting calls to unstable providers.
2. **Retry with Back-off**  
   - Retries transient errors up to `N` attempts with exponential back-off intervals.
3. **Bulkhead** (optional)  
   - Limits concurrent calls to protect downstream services.
4. **Fallback**  
   - Supplies a default or cached response when calls fail or the circuit is open.
5. **TimeLimiter** (optional)  
   - Enforces a maximum call duration.

6. **Design sketch**  
```java
// wrap a Uni with Resilience4j operators
Uni<List<T>> generate(int n) {
  return Uni.createFrom().item(() -> syncGenerate(n))
    .on().transform().byApplying(CircuitBreakerOperator.of(cbInstance))
    .on().failure().retry().with(RetryOperator.of(retryInstance))
    .onFailure().recoverWithItem(Collections.emptyList());
}
```

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

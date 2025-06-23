# Random Data Generator API

A high-performance, open-source service for generating realistic fake data on demand. Inspired by RandomUser.me and Lorem Ipsum, this project provides a RESTful and GraphQL API built with Quarkus to produce randomized user profiles, company records, credit-card details, lorem text, locations, UUIDs, animal names, and more—all localized to configurable regions without persisting any data.

# Features

- Multiple Data Types: People, companies, credit cards, lorem text, locations, UUIDs, animals, and extensible via pluggable providers.
- Dual API Interfaces: RESTful endpoints (/api/{type}) and a GraphQL endpoint (/graphql).
- Locale Awareness: Generate data in various locales (e.g., en_US, fr_FR, etc.).
- High Throughput: Designed for 10,000+ requests per second with horizontal scaling.
- Stateless & Lightweight: No database; in-memory generation ensures fast, stateless responses.
- Rate Limiting: Configurable global and per-IP rate limits via Bucket4j to prevent abuse.
- Multiple Formats: JSON by default, with CSV and XML support.
- API Documentation: Auto-generated OpenAPI/Swagger UI and GraphQL schema.
- Monitoring: Prometheus metrics exposure and Grafana dashboards integration.
- Deployment Ready: Docker, Docker Compose, and Helm charts included.
- CLI Tool: Command-line utility (randomgen) for quick local data generation.

# Quick Start

## Prerequisites

- Java 21
- Quarkus
- Maven
- Docker (for containerized run)

## Local Development

- Clone the repository
- Build the project
- Run in dev mode (Quarkus live reload)

```bash
git clone https://github.com/your-org/random-data-generator.git
cd random-data-generator

mvn clean package

# Run in dev mode (Quarkus live reload)
mvn quarkus:dev
```

The REST API will be available at http://localhost:8080/api/{type}, and Swagger UI at http://localhost:8080/openapi.

## Docker

- Build Docker image
- Run container

```bash
docker build -t random-data-generator .
docker run -p 8080:8080 random-data-generator
```

## Kubernetes

- Install with Helm

```bash
helm install random-data-generator ./helm-chart
```

## Usage Examples

Generate 5 users in French (JSON)

```bash
curl "http://localhost:8080/api/person?locale=fr_FR&count=5"

Generate 10 UUIDs (CSV)

curl "http://localhost:8080/api/uuid?format=csv&count=10"
```

## GraphQL query

```graphql
query {
  generate(type: "company", count: 3, locale: "en_US") {
    id
    name
    address
  }
}
```

## CLI tool

```bash
randomgen -t person -c 3 -l en_US
```

## Configuration

Parameters are controlled via application.properties or environment variables:

# Rate limiting
rate-limiter.global=10000
rate-limiter.per-ip=1000

# OpenAPI path
quarkus.smallrye-openapi.path=/openapi

# Security Measures

## Rate Limiting
- Global rate limit: 10,000 requests per second
- Per-IP rate limit: 1,000 requests per second
- Configurable via environment variables:
  ```properties
  rate-limiter.global=10000
  rate-limiter.per-ip=1000
  ```

## Security Headers
- Automatically added security headers:
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: DENY
  - X-XSS-Protection: 1; mode=block
  - Strict-Transport-Security: max-age=31536000; includeSubDomains

## Input Validation
- All parameters are validated for format and range
- Locale validation against supported locales
- Count parameter capped at reasonable limits

# Error Handling

## Response Format
All errors are returned in a consistent JSON format:
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": "Optional additional information"
  }
}
```

## Common Error Codes
- INVALID_REQUEST: Invalid or missing parameters
- RATE_LIMIT_EXCEEDED: Request rate too high
- INVALID_LOCALE: Unsupported locale
- INTERNAL_ERROR: Server-side error

# Examples

## REST API Examples

### Generate 5 French users
```bash
curl "http://localhost:8080/api/person?locale=fr_FR&count=5"
```

### Generate 10 UUIDs in CSV format
```bash
curl "http://localhost:8080/api/uuid?format=csv&count=10"
```

## GraphQL Examples

### Generate 3 US companies
```graphql
query {
  generate(type: "company", count: 3, locale: "en_US") {
    id
    name
    address
    email
  }
}
```

### Generate a single credit card with details
```graphql
query {
  generate(type: "creditCard", locale: "en_US") {
    cardNumber
    expiryDate
    cvv
    cardType
  }
}
```

## CLI Examples

### Generate a single US person
```bash
randomgen -t person -l en_US
```

### Generate 5 Spanish companies
```bash
randomgen -t company -c 5 -l es_ES
```

# Testing Strategy

## Unit Tests
- Each data provider has dedicated unit tests
- Validation of generated data formats
- Locale-specific data verification
- Edge case handling

## Integration Tests
- REST API endpoint testing with RestAssured
- GraphQL query validation
- Rate limiting scenarios
- Format serialization tests

## Performance Tests
- Gatling-based load testing
- Target: 10,000 requests per second
- Metrics tracked:
  - Response times (p95 < 200ms)
  - Error rates (< 0.1%)
  - Resource utilization

## Monitoring Tests
- Prometheus metrics validation
- Alert conditions testing
- Grafana dashboard verification
- Rate limiting scenarios

# Contributing

1. Fork the repository
2. Create your feature branch (git checkout -b feature/foo)
3. Commit your changes (git commit -m "feat: add foo provider")
4. Push to the branch (git push origin feature/foo)
5. Open a Pull Request

Please ensure all new features are covered by unit and integration tests.

## License

This project is licensed under the MIT License. See LICENSE for details.

# API Endpoints

This document describes all HTTP and GraphQL endpoints exposed by the Random Data Generator API.

## RESTful Endpoints

### GET /api/{type}

- Description: Generate one or more fake data records of the given type.

- Path Parameters:

    - type — the data type to generate, e.g., person, company, uuid, creditCard, lorem, location, animal, etc.

- Query Parameters:

    - locale (string, optional, default en_US) — locale/region for generated data (e.g., fr_FR, es_ES).

    - count (integer, optional, default 1) — number of records to generate (max 1000).

    - format (string, optional, one of json (default), csv, xml) — output serialization format.

- Responses:

    - 200 OK — returns data in the requested format.

    - 400 Bad Request — invalid type or parameter values.

    - 429 Too Many Requests — rate limit exceeded.

### OpenAPI / Swagger UI

- GET /openapi

- Description: Serve the aggregated OpenAPI (Swagger) specification for all REST endpoints in JSON or YAML.

- Usage: Access interactive Swagger UI at http://<host>:<port>/openapi.

- Responses:

    - 200 OK — returns OpenAPI spec.

### GraphQL Endpoint

- POST /graphql

- Description: GraphQL API for custom data generation queries.

- Request Body (JSON):
```json
{
  "query": "query { generate(type: \"company\", count: 3, locale: \"en_US\") { id name address } }"
}
```

- Response:

```json
{
  "data": {
    "generate": [
      { "id": "...", "name": "...", "address": "..." },
      { "id": "...", "name": "...", "address": "..." },
      { "id": "...", "name": "...", "address": "..." }
    ]
  }
}
```

- Errors: Standard GraphQL error responses under the errors field.

### Monitoring & Metrics

- GET /metrics

- Description: Prometheus-compatible metrics endpoint.

- Responses:

    - 200 OK — exposes application metrics (throughput, latency histograms, error rates, etc.).

## Command-Line Interface (CLI)

Note: The following is not an HTTP endpoint but demonstrates how to invoke the REST API via the bundled CLI tool.

```bash
randomgen -t person -c 5 -l en_US
```

This command issues:

    ```bash
GET /api/person?count=5&locale=en_US
```

and prints the results to stdout.


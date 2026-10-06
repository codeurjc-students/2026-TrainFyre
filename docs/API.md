# API documentation

TrainFyre exposes a REST API from the backend layer. The project uses Springdoc OpenAPI to generate API documentation automatically.

## Local access

Once the backend is running locally:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API base path

The REST API is currently exposed at the root with the following base path:

- `/incidence`

## Implemented endpoints

### Incidence management

The `IncidenceController` exposes the following endpoints for managing incidences:

**Create incidence (POST)**
```http
POST /incidence
Content-Type: application/json

{
  "affectedNetwork": { "mapId": 1, "lineIds": [1, 2, 3] },
  "occurrence": { "timestamp": "2026-10-06T10:00:00Z", "duration": "PT2H" },
  "description": { "name": "Line 5 Delay", "summary": "Delays due to technical issues" },
  "classification": { "severity": "WARNING", "cause": "TECHNICAL" }
}
```
Response: HTTP 201 Created with incidence UUID

**Get all incidences (GET)**
```http
GET /incidence?page=0&size=20
```
Response: HTTP 200 OK with paginated list of incidences

**Get incidence by ID (GET)**
```http
GET /incidence/{id}
```
Response: HTTP 200 OK with incidence details or 404 if not found

**Update incidence (PUT)**
```http
PUT /incidence
Content-Type: application/json

{
  "id": "uuid-of-incidence",
  "affectedNetwork": { ... },
  "occurrence": { ... },
  "description": { ... },
  "classification": { ... }
}
```
Response: HTTP 204 No Content

**Delete incidence (DELETE)**
```http
DELETE /incidence
Content-Type: application/json

{
  "id": "uuid-of-incidence"
}
```
Response: HTTP 204 No Content

## CORS configuration

The backend is configured to accept CORS requests from `http://localhost:5173` (the development frontend). This allows the React SPA to communicate with the backend during development.

## OpenAPI specification

The complete API specification is automatically generated from the Spring Boot annotations and is available via Swagger UI at runtime. The specification includes:

- Request/response schemas
- Parameter documentation
- HTTP status codes
- Data type definitions

## Future API expansion

As the project progresses through phases 3-5, additional endpoints for users, maps, and lines will be added. The API documentation will be updated accordingly.

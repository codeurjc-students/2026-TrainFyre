# API documentation

TrainFyre exposes a REST API from the backend layer. The project uses Springdoc OpenAPI to generate API documentation automatically.

## Local access

Once the backend is running locally:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API base path

The project currently follows a REST architecture and exposes endpoints under an API version prefix. The main patterns are:

- `/api/v1/...`

## Domain objects

The main backend domain modeled in the current implementation is centered on transport incidents, including:

- `Incidence`
- `Map`
- `Line`
- `User`

The backend module `statistics` contains the incident model and persistence logic.

## Example endpoints

```http
GET /api/v1/incidences?page=0&size=20
GET /api/v1/incidences/{id}
POST /api/v1/incidences
```

## OpenAPI specification files

The generated specification is expected to be stored under a dedicated docs area, and the backend is configured to generate OpenAPI output directly from the controllers and annotations.

The repository currently keeps the project structure aligned with the backend and frontend folders, and the runtime docs can be generated directly from the running application.

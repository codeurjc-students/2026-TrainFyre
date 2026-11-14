# REST API

TrainFyre exposes a REST API under the `/api/v1` prefix. Every URL of the API starts with that prefix.

## Documentation and tools

| Resource | Description |
| --- | --- |
| [OpenAPI specification](api/api-docs.yaml) | Specification of the API (`docs/api/api-docs.yaml`) |
| [OpenAPI documentation in HTML](https://raw.githack.com/codeurjc-students/2026-TrainFyre/main/docs/api/api-docs.html) | The same specification rendered with Swagger UI (`docs/api/api-docs.html`) |
| [Postman collection](api/TrainFyre.postman_collection.json) | One example request for every operation, with sample data |
| Swagger UI | `https://localhost/swagger-ui.html`, served by the running application |

To use the Postman collection, import `docs/api/TrainFyre.postman_collection.json`, disable the verification of SSL certificates (the local certificate is self-signed) and run one of the *Log in* requests first: they store the access token in the `token` variable, which the other requests use as a bearer token. The collection variables contain the credentials of the sample accounts of the development version (see [Execution](EXECUTION.md)).

## Authentication

`POST /api/v1/auth/login` returns a JWT access token that has to be sent in the following requests:

```http
Authorization: Bearer <accessToken>
```

The token is signed with RS256 and expires after 15 minutes. Roles are `USER` and `ADMIN`.

## Endpoints

| Method | URL | Access | Success | Description |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/login` | Public | 200 | Sign in and get an access token |
| POST | `/api/v1/users` | Public | 201 | Register a user. Returns the UUID of the new user |
| GET | `/api/v1/users/me` | Authenticated | 200 | Details of the authenticated user |
| GET | `/api/v1/users/{id}` | Owner | 200 | Details of a user |
| PUT | `/api/v1/users/me` | Authenticated | 204 | Update the authenticated user |
| PUT | `/api/v1/users/{id}` | Owner | 204 | Update a user |
| DELETE | `/api/v1/users/{id}` | Owner | 204 | Delete a user |
| GET | `/api/v1/incidence` | Public | 200 | Paginated list of incidences |
| GET | `/api/v1/incidence/{id}` | `ADMIN` | 200 | Details of an incidence |
| POST | `/api/v1/incidence` | `ADMIN` | 201 | Create an incidence. Returns its UUID |
| PUT | `/api/v1/incidence` | `ADMIN` | 204 | Update an incidence |
| DELETE | `/api/v1/incidence` | `ADMIN` | 204 | Delete an incidence |
| GET | `/api/v1/avatars/me` | Authenticated | 200 | Avatar of the authenticated user |
| GET | `/api/v1/avatars/{id}` | Owner or `ADMIN` | 200 | Avatar of a user |
| PUT | `/api/v1/avatars/me` | Authenticated | 204 | Upload the avatar of the authenticated user |
| PUT | `/api/v1/avatars/{id}` | Owner or `ADMIN` | 204 | Upload the avatar of a user |
| DELETE | `/api/v1/avatars/me` | Authenticated | 204 | Delete the avatar of the authenticated user |
| DELETE | `/api/v1/avatars/{id}` | Owner or `ADMIN` | 204 | Delete the avatar of a user |

*Owner* means that the identifier in the URL must be the one of the authenticated user. The `me` variants use the identifier of the token.

## Users and authentication

**Register**

```http
POST /api/v1/users
Content-Type: application/json

{
  "username": "new.user",
  "password": "a-long-password-123"
}
```

The username has between 3 and 32 characters (letters, digits, dot, hyphen and underscore) and the password between 12 and 128. The response is `201 Created` with the UUID of the user as the body. New users receive the `USER` role.

**Sign in**

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "dev_alice",
  "password": "TrainFyre-dev-Alice-2026!"
}
```

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Get the authenticated user**

```http
GET /api/v1/users/me
Authorization: Bearer <accessToken>
```

```json
{
  "username": "dev_alice",
  "alertSubscriptions": [],
  "roles": ["USER"]
}
```

**Update a user.** At least one of the fields must be sent. Alert subscriptions are added or removed in the same request:

```http
PUT /api/v1/users/me
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "username": "renamed.user",
  "alertSubscriptionListToAdd": [
    {
      "lines": [101, 103],
      "startTime": "2026-11-16T07:00:00+01:00",
      "endTime": "2026-11-16T09:30:00+01:00",
      "daysOfTheWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "expiration": "2027-06-30T23:59:59+02:00"
    }
  ]
}
```

To remove a subscription, send exactly the same object in `alertSubscriptionListToDelete`.

## Incidences

**List incidences** (public). The list is paginated with the `page` (default `0`) and `size` (default `300`) query parameters:

```http
GET /api/v1/incidence?page=0&size=20
```

```json
{
  "content": [
    {
      "id": "5d679c84-e9b1-5d32-8255-3dafdbe7b296",
      "affectedNetwork": { "mapId": 3, "lineIds": [306] },
      "occurrence": { "timestamp": "2026-09-03T05:10:00Z", "duration": "PT20M", "instantaneous": false },
      "description": { "name": "Technical failure on commuter rail", "summary": "Delays caused by a technical problem." },
      "classification": { "severity": "HIGH", "cause": "TECHNICAL_PROBLEM" }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 300
}
```

Each element also contains an `incidenceDetails` property that repeats the same affected network, occurrence, description and classification.

Severities are `CRITICAL`, `HIGH`, `MODERATE`, `LOW` and `INFORMATIONAL`. Causes are `WEATHER`, `MAINTENANCE`, `STRIKE`, `TECHNICAL_PROBLEM`, `MEDICAL_EMERGENCY`, `POLICE_ACTION`, `HOLIDAY` and `OTHER`. The duration uses the ISO 8601 format (`PT45M`, `PT1H30M`). An `INFORMATIONAL` incidence must have a zero duration.

**Create an incidence** (`ADMIN`):

```http
POST /api/v1/incidence
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "affectedNetwork": { "mapId": 1, "lineIds": [101, 103] },
  "occurrence": { "timestamp": "2026-11-16T08:15:00Z", "duration": "PT45M" },
  "description": { "name": "Technical failure on the metro", "summary": "Delays caused by a signalling problem." },
  "classification": { "severity": "MODERATE", "cause": "TECHNICAL_PROBLEM" }
}
```

The response is `201 Created` with the UUID of the incidence as the body.

**Update an incidence** (`ADMIN`). Only the `change*` fields that are sent are modified:

```http
PUT /api/v1/incidence
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "uuid": "5d679c84-e9b1-5d32-8255-3dafdbe7b296",
  "changeDescription": { "name": "Technical failure on the metro", "summary": "Service restored." },
  "changeClassification": { "severity": "LOW", "cause": "TECHNICAL_PROBLEM" }
}
```

**Delete an incidence** (`ADMIN`):

```http
DELETE /api/v1/incidence
Authorization: Bearer <accessToken>
Content-Type: application/json

{ "id": "5d679c84-e9b1-5d32-8255-3dafdbe7b296" }
```

## Avatars

Avatars are stored in an S3-compatible service, using the UUID of the user as the object key.

**Upload.** Multipart form with a `file` part:

```http
PUT /api/v1/avatars/me
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data; boundary=...

--...
Content-Disposition: form-data; name="file"; filename="avatar.png"
Content-Type: image/png

<binary content>
--...--
```

**Download.** `GET /api/v1/avatars/me` returns the binary content with the content type used when it was uploaded. The response is `404 Not Found` if the user has no avatar.

**Delete.** `DELETE /api/v1/avatars/me` returns `204 No Content`.

The backend stores the content type declared by the client and rejects a missing or empty file with `400 Bad Request`. The web client additionally limits avatars to PNG, JPEG and WebP images of up to 2 MB.

## Errors

| Status | When |
| --- | --- |
| 400 | The request body is invalid (for example a username or password that does not meet the constraints, or an update without modifications) or the avatar file is missing or empty |
| 401 | The access token is missing, invalid or expired, or the login credentials are wrong |
| 403 | The authenticated user does not have the required role |
| 404 | The requested user or avatar does not exist |

Errors raised by the user and authentication endpoints are returned as `application/problem+json` (`401` invalid credentials, `403` access denied to another user's account, `404` resource not found).

The avatar endpoints only translate the missing file (`400`) and the missing avatar (`404`). When a user tries to read, upload or delete the avatar of another user without being an administrator, the application layer rejects the operation with `FindAvatarDeniedException` or `ChangeAvatarDeniedException`, but no handler maps these exceptions to an HTTP status yet, so the response is the default server error and not a `403`. Likewise, a request for an incidence that does not exist is not mapped to `404` by the incidence endpoints.

## CORS

The backend accepts requests from the origins listed in the `TRAINFYRE_CORS_ALLOWED_ORIGINS` environment variable (default `http://localhost:5173`, the Vite development server) with the methods `GET`, `POST`, `PUT`, `DELETE` and `OPTIONS` and the headers `Authorization` and `Content-Type`. The compiled application is served from the same origin as the API.

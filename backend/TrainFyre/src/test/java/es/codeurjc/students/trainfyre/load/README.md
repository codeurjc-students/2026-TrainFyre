# TrainFyre Load Tests (Gatling + Garage)

This folder contains the Gatling simulation for the real TrainFyre API. It does not start PostgreSQL, Garage, LocalStack, or the Spring Boot application. Start your existing Docker Compose stack first and run Gatling against the published backend port.


## Important: use the real Garage service

The avatar journey calls the TrainFyre API, not Garage directly. The backend then uses the S3-compatible Garage service through the Docker Compose network. This measures the complete application path: HTTP request, Spring controller/service, S3 client, Garage, and response.

The Docker Compose configuration supplied for this project maps the dependencies as follows:

| Setting | Value from the Compose configuration |
|---|---|
| Backend URL from the host | `https://localhost:443` |
| Garage endpoint from the backend container | `http://garage:3900` |
| S3 region | `garage` |
| S3 bucket | `${GARAGE_DEFAULT_BUCKET}` |
| S3 access key | `${GARAGE_DEFAULT_ACCESS_KEY}` |
| S3 secret key | `${GARAGE_DEFAULT_SECRET_KEY}` |
| S3 path-style access | `true` |
| Garage image | `dxflrs/garage:v2.5.0` |

Gatling does **not** need Garage credentials, the Garage RPC secret, the Garage RPC address, or direct access to port `3900`. Keep those settings in Docker Compose / the backend environment only.

The Compose file requires the following environment variables to start the stack:

- `DB_PASSWORD`
- `GARAGE_DEFAULT_ACCESS_KEY`
- `GARAGE_DEFAULT_SECRET_KEY`
- `GARAGE_DEFAULT_BUCKET`
- `TRAINFYRE_TLS_PASSWORD`

`DOCKERHUB_USERNAME` is optional in the supplied Compose configuration. Keep the actual values in your local `.env` file; do not commit them or copy them into Gatling configuration.

## First run: smoke test

Run from the Maven project root. Supply a real account with the `ADMIN` role:

```bash
./mvnw gatling:test \\
  -Dgatling.simulationClass=es.codeurjc.students.trainfyre.load.TrainFyreLoadSimulation \\
  -DloadProfile=smoke \\
  -DbaseUrl=https://localhost:443 \\
  -DloadAdminUsername=YOUR_ADMIN_USERNAME \\
  -DloadAdminPassword=YOUR_ADMIN_PASSWORD \\
  -DincludeAvatarTests=true
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

The avatar journey is enabled by default because the supplied Compose configuration already wires the backend to Garage. You can disable it explicitly with `-DincludeAvatarTests=false` when testing only the non-storage API paths.

## Load profiles

| Profile | Workload | Purpose |
|---|---|---|
| `smoke` | One virtual user per enabled journey | Validate credentials, API permissions, and Garage-backed avatar operations. |
| `realistic` | Gradual ramp-up followed by a constant arrival rate | Establish a representative baseline. |
| `stress` | Three increasing arrival-rate stages, up to four times the configured rate | Find the saturation point. |
| `spike` | Baseline traffic, a rapid increase up to five times the rate, then ramp-down | Observe behavior and recovery after sudden peaks. |
| `soak` | Constant arrival rate for a longer period | Detect degradation, resource leaks, and connection-pool exhaustion. |

### Realistic profile

```bash
./mvnw gatling:test \\
  -Dgatling.simulationClass=es.codeurjc.students.trainfyre.load.TrainFyreLoadSimulation \\
  -DloadProfile=realistic \\
  -DbaseUrl=https://localhost:443 \\
  -DloadAdminUsername=YOUR_ADMIN_USERNAME \\
  -DloadAdminPassword=YOUR_ADMIN_PASSWORD \\
  -DrampUpSeconds=60 \\
  -DdurationSeconds=180 \\
  -DincludeAvatarTests=true
```

### Stress profile

```bash
./mvnw gatling:test \\
  -Dgatling.simulationClass=es.codeurjc.students.trainfyre.load.TrainFyreLoadSimulation \\
  -DloadProfile=stress \\
  -DloadFactor=1.0 \\
  -DstageSeconds=90 \\
  -DbaseUrl=https://localhost:443 \\
  -DloadAdminUsername=YOUR_ADMIN_USERNAME \\
  -DloadAdminPassword=YOUR_ADMIN_PASSWORD \\
  -DincludeAvatarTests=true
```

### Spike profile

```bash
./mvnw gatling:test \\
  -Dgatling.simulationClass=es.codeurjc.students.trainfyre.load.TrainFyreLoadSimulation \\
  -DloadProfile=spike \\
  -DbaseUrl=https://localhost:443 \\
  -DloadAdminUsername=YOUR_ADMIN_USERNAME \\
  -DloadAdminPassword=YOUR_ADMIN_PASSWORD \\
  -DincludeAvatarTests=true
```

### Soak profile (20 minutes by default)

```bash
./mvnw gatling:test \\
  -Dgatling.simulationClass=es.codeurjc.students.trainfyre.load.TrainFyreLoadSimulation \\
  -DloadProfile=soak \\
  -DsoakDurationSeconds=1200 \\
  -DbaseUrl=https://localhost:443 \\
  -DloadAdminUsername=YOUR_ADMIN_USERNAME \\
  -DloadAdminPassword=YOUR_ADMIN_PASSWORD \\
  -DincludeAvatarTests=true
```

## Workload model

Rates represent new user journeys per second, not raw HTTP requests per second. Default `realistic` rates are:

| Journey | Journeys per second | Requests and behavior |
|---|---:|---|
| Public browsing | 8.0 | Three paginated incidence-list requests per visit, with 1–3 second think time. Page sizes vary between 10, 20, 50, and 300. |
| Incidence details | 2.0 | Admin login followed by a detail request for an existing incidence. |
| User profile | 1.0 | Admin login followed by `GET /api/v1/users/me`. |
| Login | 0.5 | Login request only. |
| Incidence CRUD | 0.25 | Admin login, create, read, update, and delete. |
| User lifecycle | 0.10 | Register, log in, read own profile, update own username, and delete own account. |
| Avatar lifecycle | 0.02 | Register, log in, multipart upload, read avatar, delete avatar, and delete user. The upload payload is a generated 256×256 PNG (approximately 200 KB). |

Before load starts, the simulation reads existing incidence IDs and creates enough temporary test incidences to reach `seedIncidenceCount` (100 by default). It removes only the setup incidences it created. Incidence CRUD and user lifecycle journeys delete their own records on successful completion.

## Configuration properties

| Property | Default | Description |
|---|---:|---|
| `baseUrl` | `https://localhost:443` | Backend base URL as reachable from the Gatling host. |
| `loadAdminUsername` | `admin` | Account used for admin-only incidence operations. Override it with a real test admin. |
| `loadAdminPassword` | `admin123456789` | Password for the configured admin account. Override it; do not use production credentials. |
| `loadNewUserPassword` | `LoadTestUser1234!` | Password assigned to generated test users. |
| `loadProfile` | `realistic` | `smoke`, `realistic`, `stress`, `spike`, or `soak`. |
| `loadFactor` | `1.0` | Multiplies all journey arrival rates. |
| `browseRate` | `8.0` | Public browsing journeys per second. |
| `detailRate` | `2.0` | Incidence detail journeys per second. |
| `profileRate` | `1.0` | Profile journeys per second. |
| `loginRate` | `0.5` | Login-only journeys per second. |
| `incidenceCrudRate` | `0.25` | Incidence CRUD journeys per second. |
| `userLifecycleRate` | `0.10` | User lifecycle journeys per second. |
| `avatarRate` | `0.02` | Garage-backed avatar journeys per second. |
| `includeAvatarTests` | `true` | Enables the Garage-backed avatar scenario. Set to `false` to disable it. |
| `rampUpSeconds` | `60` | Initial ramp duration for `realistic`. |
| `durationSeconds` | `180` | Constant-rate duration for `realistic`. |
| `stageSeconds` | `60` | Duration of each `stress` stage. |
| `soakDurationSeconds` | `1200` | Duration of `soak`. |
| `seedIncidenceCount` | `100` | Minimum incidence count prepared before load starts (at least 10). |
| `maxFailedPercent` | `2.0` | Maximum allowed global failed-request percentage. |
| `maxP95Millis` | `1000` | Maximum global p95 response time in milliseconds. |
| `maxP99Millis` | `2000` | Maximum global p99 response time in milliseconds. |

## What is measured for Garage

The avatar journey measures these API operations separately in the Gatling report:

1. `PUT /api/v1/avatars/me` — multipart upload to Garage through the backend S3 client.
2. `GET /api/v1/avatars/me` — read the avatar through the backend.
3. `DELETE /api/v1/avatars/me` — remove the avatar through the backend.

Gatling measures end-to-end API latency, not Garage's internal latency in isolation. To isolate Garage as the bottleneck, correlate these request timings with backend CPU/heap, application logs, and Garage/container resource metrics. The upload size is approximately 200 KB; tune it in `createTemporaryAvatarFile()` if your real avatar policy uses a different maximum size.

## Reports and bottleneck analysis

Gatling writes an HTML report under `target/gatling/`. Open the `index.html` file for the latest run.

Review:

- p50, p95, and p99 latency by request name to identify the first endpoint that degrades.
- Requests per second and active virtual users to see whether throughput keeps scaling.
- Failure percentages and HTTP status codes to distinguish authorization/data errors from server errors.
- Latency over time, correlated with application CPU, heap, garbage collection, thread counts, and database connection-pool metrics.
- Avatar upload/read/delete timings alongside Garage container CPU, memory, disk I/O, and network throughput.

Monitor PostgreSQL for active connections, slow queries, locks, and connection-pool wait time. Run Gatling on a different machine from the backend for capacity measurements so the load generator does not compete for backend CPU and memory.

## Safety and data cleanup

Run these tests only against a disposable test environment. They create and delete incidences and users, and the avatar scenario writes and deletes objects in the configured Garage bucket. Do not point the tests at production. If a journey fails mid-way, check for leftover temporary users or incidences before the next run.

## Java compatibility note

The project POM shown for TrainFyre declares Java 27, while Gatling 3.16's published compatibility documentation lists supported JDK versions through 25. Keep this compatibility issue in mind when launching Maven; the load test should run on a JDK officially supported by the selected Gatling release, and any Java-version change must also be compatible with the backend source code.

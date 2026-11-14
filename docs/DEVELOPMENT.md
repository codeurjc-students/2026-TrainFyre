# Development guide

This guide explains how TrainFyre is built and how to work on its code. To only run the application, see [Execution](EXECUTION.md).

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Tools](#tools)
- [Architecture](#architecture)
- [Quality control](#quality-control)
- [Deployment](#deployment)
- [Development process](#development-process)
- [Running and editing the code](#running-and-editing-the-code)

## Introduction

TrainFyre is a web application with a **SPA (single-page application) architecture**. The browser downloads a React application once and, from then on, navigates without reloading the page: every view is rendered on the client and the data are requested to a REST API in JSON format. The application has three parts:

- a **client** (React, TypeScript), which is compiled to static files;
- a **server** (Spring Boot), which exposes the REST API and also serves the static files of the client;
- a **database** (PostgreSQL), plus an S3-compatible **object storage** where the avatars are kept.

| | |
| --- | --- |
| **Type** | SPA with a REST API. Modular monolith on the server |
| **Technologies** | Java 27, Spring Boot 4.1, React 19, TypeScript, PostgreSQL 18, S3 storage (Garage in development) |
| **Tools** | IntelliJ IDEA, WebStorm, Git and GitHub, Maven, npm and Vite, Docker and Docker Compose, Figma, Postman |
| **Quality control** | JUnit, Mockito, Testcontainers, Rest Assured, Vitest, Playwright, JaCoCo, SonarQube Cloud, Spring Modulith architecture tests |
| **Deployment** | One Docker image with the client and the server, published in DockerHub, plus a Docker Compose file that adds PostgreSQL |
| **Development process** | Iterative and incremental, GitHub Flow, continuous integration and delivery with GitHub Actions |

## Technologies

Technologies used by the application at run time.

### Server

| Technology | Use in TrainFyre |
| --- | --- |
| [Java 27](https://openjdk.org/) | Language and runtime |
| [Spring Boot 4.1](https://spring.io/projects/spring-boot) | Application framework, embedded web server, configuration |
| [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html) | REST controllers |
| [Spring Security](https://spring.io/projects/spring-security) | Stateless authentication and authorization with JWT (OAuth2 resource server) and Argon2 password hashing |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) and Hibernate | Persistence in PostgreSQL |
| [Spring Modulith](https://spring.io/projects/spring-modulith) | Verification of the module boundaries |
| [jMolecules](https://github.com/xmolecules/jmolecules) | Annotations of the domain-driven design and hexagonal architecture concepts |
| [springdoc-openapi](https://springdoc.org/) | Swagger UI served by the running application |
| [AWS SDK for Java v2 (S3)](https://aws.amazon.com/sdk-for-java/) | Client of the S3-compatible object storage |
| [Lombok](https://projectlombok.org/) | Generation of boilerplate code |
| [PostgreSQL 18](https://www.postgresql.org/) | Relational database |
| [Garage](https://garagehq.deuxfleurs.fr/) | S3-compatible object storage used in development |

### Client

| Technology | Use in TrainFyre |
| --- | --- |
| [React 19](https://react.dev/) | User interface components |
| [TypeScript](https://www.typescriptlang.org/) | Typed language of the client |
| [Vite](https://vite.dev/) | Development server and production build |
| [Inter](https://fontsource.org/fonts/inter) font through `@fontsource-variable/inter` | Typography |

The client does not use a UI component library, a router or an HTTP library: styles are written in plain CSS (`src/App.css`), routes are resolved from the URL hash and requests use the browser `fetch` API. The build adds a Content-Security-Policy and other security headers.

## Tools

| Tool | Use |
| --- | --- |
| [IntelliJ IDEA](https://www.jetbrains.com/idea/) | IDE for the server (Java) |
| [WebStorm](https://www.jetbrains.com/webstorm/) | IDE for the client (TypeScript and React) |
| [JetBrains Air](https://www.jetbrains.com/) | Environment to work with the AI agents in the IDEs (see the [AI usage log](AI_USAGE.md)) |
| [Git](https://git-scm.com/) and [GitHub](https://github.com/) | Version control and repository hosting |
| [GitHub Issues and Projects](https://docs.github.com/en/issues) | Task management with a Kanban board |
| [GitHub Actions](https://docs.github.com/en/actions) | Continuous integration and delivery |
| [Maven](https://maven.apache.org/) (Maven Wrapper) | Build of the server |
| [Node.js 24](https://nodejs.org/) and [npm](https://www.npmjs.com/) | Build and tests of the client |
| [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) | Packaging and local execution |
| [DockerHub](https://hub.docker.com/) | Registry of images and Compose files |
| [ORAS](https://oras.land/) | Publication of the Compose file as an OCI artifact |
| [Testcontainers](https://testcontainers.com/) | PostgreSQL and MariaDB containers in the server tests |
| [Gatling](https://gatling.io/) | Load tests of the API |
| [SonarQube Cloud](https://sonarcloud.io/) | Static analysis and coverage |
| [Figma](https://www.figma.com/) | Design of the screens (see [Analysis](ANALYSIS.md)) |
| [Postman](https://www.postman.com/) | Calls to the REST API with the [collection](api/TrainFyre.postman_collection.json) |
| [OpenSSL](https://www.openssl.org/) | Creation of the local certificate and the JWT keys |


## Architecture

The architecture is described in a separate document: [Architecture](ARCHITECTURE.md). It contains:

- the deployment architecture,
- the REST API and its [OpenAPI documentation](https://raw.githack.com/codeurjc-students/2026-TrainFyre/main/docs/api/api-docs.html),
- the domain model,
- the server architecture (hexagonal layers and modules),
- the client architecture.

## Quality control

Automated tests, coverage and static analysis are described in [Quality control](TESTING.md). In short:

- the server has unit, controller, integration (Testcontainers), system (Rest Assured) and architecture tests;
- the client has unit, component and integration tests (Vitest) and end-to-end tests (Playwright);
- the coverage of both parts is analysed in SonarQube Cloud.

Code conventions applied in the project: code and comments in English, with user-facing texts of the interface in Spanish; the server follows the hexagonal layering so that controllers never access repositories; and server tests are written together with the code (see the development process below).

## Deployment

TrainFyre is packaged in **one Docker image** that contains the compiled client and the server. The image is built with `docker/Dockerfile`, listens on HTTPS port 443 and is published in DockerHub:

- Image: `escipion48/trainfyre` (tags `dev`, a version such as `0.1.0`, and `latest`)
- Compose files as OCI artifacts: `escipion48/trainfyre-compose`

Docker Compose coordinates the containers:

| File | Containers | Use |
| --- | --- | --- |
| `docker/docker-compose.yml` | application (published image), PostgreSQL | Run the latest published version |
| `docker/docker-compose-dev.yml` | application (built from the sources), PostgreSQL, Garage | Run the development version with sample data |

The configuration is passed with environment variables defined in `docker/.env` (template in `docker/.env.example`). The database container has a health check and the application starts only when it passes. How the images are published is explained in [CI/CD and releases](CI_CD.md). No cloud deployment exists in version 0.1.

## Development process

The process is iterative and incremental, follows the principles of the agile manifesto and uses practices of Extreme Programming (small steps, tests written with the code, continuous integration) and Kanban (visual board of tasks). It does not apply Scrum.

### Task management

Every functionality or fix is a GitHub issue, tracked in the [GitHub Project](https://github.com/orgs/codeurjc-students/projects/52) with a Kanban board. See [Project tracking](PROJECT_TRACKING.md).

### Git

The repository follows [GitHub Flow](https://docs.github.com/en/get-started/using-github/github-flow): `main` is the stable branch, work is done in short-lived branches (named after the task, for example `43-add-spring-security`, `feature/frontend` or `fix/sonarqube-coverage-calculation`) and they are merged into `main` through pull requests. Commit messages follow the Conventional Commits style (`feat`, `fix`, `test`, `refactor`, `docs`) with the affected layer as scope, and tests are normally committed before the code that makes them pass.

Metrics of the `51-alignment-with-project-evaluation-guidelines` branch, measured on 2026-11-14: 424 commits and 9 merged pull requests.

### Continuous integration and delivery

The workflows of GitHub Actions are described in [CI/CD and releases](CI_CD.md): a basic control on every push to a feature branch, a complete control with static analysis on pull requests to `main`, and the publication of the Docker artifacts on every merge to `main`, release and manual build.

### Versioning

Releases and the way versions are set in `pom.xml`, `package.json` and the Compose file are explained in [CI/CD and releases](CI_CD.md#versioning-and-releases).

## Running and editing the code

### Clone the repository

```bash
git clone https://github.com/codeurjc-students/2026-TrainFyre.git
cd 2026-TrainFyre
```

### Project layout

```text
.
├── backend/TrainFyre/        Spring Boot project (pom.xml, mvnw, src/main, src/test)
├── frontend/                 React project (package.json, src, tests, e2e)
├── docker/                   Dockerfile, Compose files, .env.example, Garage configuration
├── docs/                     Documentation and OpenAPI files (docs/api)
├── .github/workflows/        GitHub Actions workflows
├── README.md
├── CHANGELOG.md
└── sonar-project.properties
```

### Start the server and the database

The simplest way is Docker Compose, which also starts the object storage. Create the credentials and the `docker/.env` file as explained in [Execution](EXECUTION.md) (steps 2 and 3) and run:

```bash
docker compose --env-file docker/.env -f docker/docker-compose-dev.yml up -d --build
```

The server is then available at <https://localhost> and loads the sample data. After changing server code, run the command again to rebuild the image.

### Start the client in development mode

Requirements: Node.js 24.

```bash
cd frontend
npm ci
npm run dev
```

The client is served at <http://localhost:5173> and calls the API at the address defined in `frontend/.env` (`VITE_API_BASE_URL`, `https://localhost:443/api/v1` by default). The backend accepts the origin `http://localhost:5173` through CORS. Open <https://localhost> once in the browser and accept the self-signed certificate, otherwise the browser blocks the requests of the client.

Other useful commands:

```bash
npm run build       # type checking and production build
npm run typecheck   # TypeScript only
npm run lint        # oxlint
```

### Work with the server code

Requirements: JDK 27 and Docker (the integration and system tests use Testcontainers).

```bash
cd backend/TrainFyre
./mvnw verify        # compile and run every test (on Windows: mvnw.cmd)
```

`ArchitectureTest` writes the Spring Modulith documentation of the modules to `target/spring-modulith-docs`.

### Call the REST API

Import the [Postman collection](api/TrainFyre.postman_collection.json) in Postman. It contains an example request for every operation, with sample data, and stores the token after logging in. The operations are listed in [REST API](API.md). With the development version running, Swagger UI is also available at <https://localhost/swagger-ui.html>.

### Run the tests

The commands to run each type of test are in [Quality control](TESTING.md#running-the-tests).

### Create a release

The steps are in [CI/CD and releases](CI_CD.md#versioning-and-releases).

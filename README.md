# TrainFyre: A web application for real-time transport alert management

## Description

A system capable of collecting data from various transport sources. 
This data will be used and processed to provide various functionalities, 
such as allowing users to receive notifications regarding lines of interest
and statistics related to those lines.

## Objectives

The objectives of this project center on the ability to collect and process
alerts from various sources in order to offer a range of services to users similar
to those provided by official websites like [Metro Madrid](https://www.metromadrid.es/es) 
and [Renfe](https://www.adif.es/viajeros/estado-de-la-red), or apps like [Moovit](https://moovitapp.com/es).

### Functional objectives

The functional objectives will be delivered in a phased manner across iterations.
Some functional objectives may change in the future.

These functional objectives/features will be accessible based on user roles:
anonymous, registered, and administrator. These are listed in order of privilege,
with administrators having access to all available features.

The project's functional objectives are listed below, indicating the minimum
privilege level (role) required for access:

* Register in the system (anonymous).
* View general statistics for the lines or their status (anonymous).
* Create alert subscriptions to subsequently receive notifications (registered).
* Log in and log out (registered).
* CRUD operations on entities other than the user (admin).
* View or collect audit information and statistics on the system and/or lines (admin).

### Technical objectives

The technical objectives focus on building a maintainable and extensible monolithic application capable of collecting,
processing, and managing transport alerts from multiple sources.
The system will follow established software engineering principles and architectural patterns to ensure low coupling, testability, and maintainability.

* **Modular monolithic architecture based on Hexagonal Architecture (Ports and Adapters)**, separating the domain logic from infrastructure and external technologies.
* **Domain-Driven Design (DDD) and Event-Driven Architecture (EDA)** principles to model the transport domain and decouple application components following a monolith-first approach.
* **Integration of multiple data sources** through dedicated adapters, together with the ingestion, normalization, and processing of heterogeneous data into a consistent internal representation.
* **Full-stack web application**, with a **Java and Spring Boot** backend exposing a RESTful API and a **React** frontend implemented as a Single-Page Application (SPA).
* **Authentication and authorization** using **JWT tokens and role-based access control**, with different permissions for anonymous, registered, and administrator users.
* **Alert subscription and notification management**, allowing registered users to subscribe to alerts related to selected transport lines and receive notifications by email.
* **Persistence and management of application data**, including the information required by the application and its audit capabilities.
* **Automated testing following TDD principles**, promoting maintainability and reliability throughout development.
* **Static code analysis and software quality control** using **SonarQube**.
* **Containerization using Docker** to provide reproducible development and deployment environments.

## Methodology

Work will be carried out in iterations, meeting objectives in a phased manner
that allows for changes. Similarly, only dates for key milestones will be scheduled,
and work completed for each delivery (one delivery per milestone) will be recorded,
capturing the start and end dates of tasks performed during the iteration.

Given the complexity and difficulty of the project's objectives,
sound software design principles, patterns, and practices will be followed
to the extent possible; these are:

* Hexagonal architecture: It is useful, as it will receive data from various sources.
  Furthermore, its modularity will facilitate testing and maintenance.
* DDD (domain driven design): improving communication and maintainability
  (ubiquitous language), enabling the creation of a more modular and maintainable system
* EDA (Event driven architecture): Enabling the reduction of dependencies between
  services and their subsequent division into microservices (monolith-first approach).
* TDD (test driven development): improving software design,
  drastically reducing errors, and enabling code modification without fear,
  thanks to a safety net of automated tests created before the source code

A Gantt chart will be added at the end of the project to properly showcase the 
phases, for the moment the project has the following table:

| Phase   | Description                                  | Deadline     |
| ------- | -------------------------------------------- | ------------ |
| Phase 1 | Definition of functionalities and screens    | 15 September |
| Phase 2 | Repository, testing and CI                   | 15 October   |
| Phase 3 | Version 0.1 - Basic functionality and Docker | 15 December  |
| Phase 4 | Version 0.2 - Intermediate functionality     | 1 March      |
| Phase 5 | Version 1.0 - Advanced functionality         | 15 April     |
| Phase 6 | Report                                       | 15 May       |
| Phase 7 | Defense                                      | 15 June      |

You can see the current project status on its related GitHub project on: [TrainFyre GitHub Project](https://github.com/orgs/codeurjc-students/projects/52)

## Detailed features

The following table summarizes the main functionalities planned for TrainFyre and the minimum user role required to access them.

|                  | Anonymous                                                               | Registered                                                      | Admin                             |
| ---------------- | ----------------------------------------------------------------------- | --------------------------------------------------------------- | --------------------------------- |
| **Basic**        | Register in the system                                                  | Log in and log out and upload and update his profile pictures   | Manage system entities through CRUD |
| **Intermediate** | View the current status of transport lines and general alert statistics | Create and manage alert subscriptions and receive notifications | View system and transport alert statistics |
| **Advanced**     | TODO                                                                    | TODO                                                            | TODO                              |

> [!NOTE]
> The advanced functionalities are not yet defined and will be specified as the project progresses. The features listed in the table may also be refined or extended during development.

## Analysis

This section documents the current analysis and design decisions made for the project.
As TrainFyre is being developed following an iterative and incremental methodology,
the level of detail in this section will evolve as the project progresses.

### Screens and navigation

The initial user interface has been designed as a prototype using Figma:

[Website layout](https://www.figma.com/design/4TL76NTkyb7FL6PTQH29iN/Trainfyre-maquetaci%C3%B3n?node-id=305-5749&t=TcFFmUE4bhh7lFds-1)

The prototype defines the main navigation structure and the screens required for the currently identified basic and intermediate functionality.

### Entities

The current domain model is centered around transport lines, maps, and users following Domain-Driven Design (DDD) principles.

The main entities currently identified are:

* **User**, representing a registered user of the application.
* **Line**, representing a transport line managed by the application.
* **Map**, representing a collection of transport lines.
* **Incidence**, representing transport alerts or incidents affecting one or more lines.

### User permissions

The application defines three user roles:

* **Anonymous users** can access public information, including transport line status and general alert-related statistics, and can register an account.
* **Registered users** can authenticate, manage their alert subscriptions, and receive notifications related to the transport lines they are interested in.
* **Administrators** have access to management operations over application entities, as well as complete system and audit information access.

### Images

The application supports image uploads. Currently, the **User** entity is confirmed to have an associated image (profile picture).

### Charts

Charts will be used to represent statistical information related to transport lines and alerts, such as:

* The number of alerts associated with a transport line over a given time period.
* The distribution of alerts across different transport lines.
* Aggregated information about alerts that can help users understand the current or historical status of the transport network.

### Complementary technology

Email notifications have been identified as the first complementary technology for the application. Registered users will be able to subscribe to transport lines and receive notifications when relevant alerts are detected.

### Algorithm or advanced query

The application will include advanced data processing or querying beyond basic CRUD operations, such as filtering and aggregating alerts by transport line and time period.

---

# Development Guide

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technologies](#technologies)
- [Tools](#tools)
- [Quality Control](#quality-control)
- [Development Process](#development-process)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)

## Architecture Overview

TrainFyre is a full-stack web application built with a modern architecture following software engineering best practices:

**Architecture Type:** Single-Page Application (SPA) with a RESTful backend

**System Components:**
- **Frontend:** React-based SPA running in the browser
- **Backend:** Spring Boot REST API handling business logic
- **Database:** PostgreSQL for data persistence
- **Container Platform:** Docker for reproducible environments

**Communication Protocol:** HTTP/HTTPS

The application follows a **Hexagonal Architecture** (Ports and Adapters) pattern that cleanly separates domain logic from infrastructure concerns, making the codebase more testable and maintainable. **Spring Modulith** enforces modular structures at the architectural level, preventing unwanted dependencies between modules.

### Architecture Summary

| Aspect | Details |
| --- | --- |
| **Type** | SPA (Single-Page Application) with REST API |
| **Technologies** | Java 26, Spring Boot 4.1.1, React 19, TypeScript, PostgreSQL 16 |
| **Herramientas** | Maven (backend), npm/Vite (frontend), Docker, Docker Compose |
| **Quality Control** | JUnit + Vitest unit tests, integration tests, E2E tests (Playwright), static analysis (SonarQube Cloud) |
| **Deployment** | Docker containers, GitHub Actions CI/CD pipeline |

## Technologies

### Backend

**Java 26** | https://www.java.com/

The application uses Java 26, the latest long-term support version providing modern language features and performance improvements.

**Spring Boot 4.1.1** | https://spring.io/projects/spring-boot

Spring Boot provides rapid development of production-grade applications with an embedded servlet container and comprehensive ecosystem of libraries. The application uses:

- **spring-boot-starter-webmvc**: HTTP request handling and REST API development
- **spring-boot-starter-data-jpa**: Object-relational mapping and database access
- **spring-boot-devtools**: Hot reload and development enhancements
- **spring-security**: Authentication and authorization (planned)

**Spring Modulith** | https://spring.io/projects/spring-modulith

Enforces modular architecture at the code level by validating module boundaries, preventing circular dependencies, and documenting module interactions.

**jMolecules** | https://jmolecules.org/

Provides annotations and domain-driven design abstractions (@AggregateRoot, @Entity, @ValueObject) that communicate design intent without imposing implementation constraints.

**PostgreSQL 16** | https://www.postgresql.org/

Robust relational database system used for persistent storage of domain entities and audit information.

**Springdoc OpenAPI 3.1.1** | https://springdoc.org/

Auto-generates OpenAPI documentation from Spring Boot controllers, exposing interactive API documentation at runtime.

### Frontend

**React 19.2.8** | https://react.dev/

Modern JavaScript library for building interactive user interfaces as components. Uses hooks for state and effect management and integrates with the backend via REST API calls.

**TypeScript 6.0** | https://www.typescriptlang.org/

Typed superset of JavaScript providing static type checking, improving code quality and developer experience.

**Vite 8.3.0** | https://vitejs.dev/

Modern frontend build tool providing fast development server with hot module replacement (HMR) and optimized production builds.

**Tailwind CSS 4.3.3** | https://tailwindcss.com/

Utility-first CSS framework for rapid UI development with responsive design support.

**shadcn/ui** | https://ui.shadcn.com/

Accessible, customizable component library built on top of Radix UI and Tailwind CSS.

**Recharts 3.8.0** | https://recharts.org/

React component library for building composable, responsive charts and graphs.

**Axios 1.20.0** | https://axios-http.com/

Promise-based HTTP client for making REST API requests from the frontend.

## Tools

### Development Environment

**Maven 3.9** | https://maven.apache.org/

Build automation tool for the Java backend. Manages dependencies, compiles code, runs tests, and packages the application. Configuration is defined in `pom.xml`.

**npm** | https://www.npmjs.com/

Package manager for JavaScript/Node.js dependencies. Frontend dependencies and scripts are defined in `package.json`.

**Visual Studio Code** | https://code.visualstudio.com/

Recommended IDE for both backend and frontend development. Install extensions for Java, Spring Boot, JavaScript/TypeScript, and Docker.

**IntelliJ IDEA** | https://www.jetbrains.com/idea/

Full-featured IDE with excellent Spring Boot and Java support (Community Edition available).

### Testing Tools

**JUnit 5** | https://junit.org/

Testing framework for Java used for unit, integration, and end-to-end tests on the backend.

**Mockito** | https://site.mockito.org/

Mocking library for Java tests, allowing isolated testing of components by stubbing external dependencies.

**Testcontainers** | https://testcontainers.com/

Manages Docker containers for integration tests, providing isolated database instances (PostgreSQL, MariaDB) without manual setup.

**Vitest** | https://vitest.dev/

Fast JavaScript test runner with Jest-compatible API. Used for unit and integration testing of frontend components.

**Testing Library** | https://testing-library.js.org/

Utilities for testing React components by simulating user interactions and verifying rendered output.

**Playwright** | https://playwright.dev/

End-to-end testing framework for testing the application through the browser, ensuring full user workflows function correctly.

**JaCoCo** | https://www.jacoco.org/

Code coverage measurement tool for Java. Reports are generated automatically during the Maven build and uploaded to SonarQube Cloud.

### Continuous Integration & Quality

**GitHub Actions** | https://github.com/features/actions

Automated workflow system integrated into GitHub. Runs tests, code analysis, and other checks on every push and pull request.

**SonarQube Cloud** | https://www.sonarsource.com/products/sonarqube/

Static code analysis platform that scans for code smells, security vulnerabilities, bugs, and measures code coverage. Integrates with GitHub to comment on pull requests.

**Docker & Docker Compose** | https://www.docker.com/

Containerization platform ensuring consistent development and testing environments across all machines.

## Quality Control

### Automated Testing Strategy

Tests are organized by type and scope, ensuring comprehensive coverage from individual functions to complete user workflows:

#### Backend Testing

| Test Type | Framework | Scope | Dependencies | Command |
|---|---|---|---|---|
| **Unit** | JUnit 5 + Mockito | Domain, services, commands/queries | Mocked | `mvn test -Dtest='!*IntegrationTest,!*E2ETest'` |
| **Integration** | JUnit 5 + Testcontainers | Service + Repository layers | Real database | `mvn verify` |
| **E2E** | JUnit 5 + Rest Assured | REST API endpoints | Real database | `mvn verify` |

#### Frontend Testing

| Test Type | Framework | Scope | Dependencies | Command |
|---|---|---|---|---|
| **Unit** | Vitest + Testing Library | Components, hooks, services | Mocked | `npm run test:unit` |
| **Unit + Coverage** | Vitest + V8 | Components, hooks, services | Mocked | `npm run test:coverage` |
| **Integration** | Vitest + Testing Library | Client-server communication | Real API | `npm run test:integration` |
| **E2E** | Playwright | Full user workflows | Real API | `npm run test:e2e` |

### Test Coverage Requirements

- Backend: Target minimum 80% code coverage (enforced for main branch)
- Frontend: Target 100% coverage for `src/**` and `components/layout/**` (excluding generated UI components)

### Static Code Analysis

**SonarQube Cloud** automatically analyzes code quality on every pull request:

- Detects code smells and duplications
- Identifies security vulnerabilities and hotspots
- Measures code coverage from JaCoCo (backend) and V8 (frontend)
- Provides quality gate status blocking merges if standards are not met

## Development Process

### Version Control Strategy

The project uses **GitHub Flow** for managing code changes:

**Main Branch:** `main`
- Always contains production-ready code
- Protected branch requiring pull request reviews
- Triggered full test suite before merge

**Feature Branches:** `feature/*`, `fix/*`
- Created from `main` for each new feature or bug fix
- Descriptive names in English (e.g., `feature/add-login-page`, `fix/navigation-bug`)
- Unit tests run automatically on each commit

**Integration:** Changes are integrated via pull requests with the following workflow:
1. Create feature branch and commit changes
2. Push branch and open pull request to `main`
3. Full test suite runs automatically (backend + frontend + SonarQube)
4. Code review required before merge
5. Squash and merge to `main` upon approval

### Task Management

**GitHub Issues** organize project work:
- One issue per feature, bug fix, or task
- Issues reference corresponding entities and requirements
- Updated with progress and linked to pull requests

**GitHub Projects** provides a Kanban-style board for visual workflow management:
- Tasks move through columns: Backlog → In Progress → In Review → Done
- Automated based on issue status and pull request activity

### Git Commit Guidelines

Write clear, concise commit messages describing the change purpose:

```
type(scope): brief description

- Detailed explanation of what changed and why
- Organized as bullet points if multiple aspects
```

**Types:** `feat` (feature), `fix` (bug fix), `docs` (documentation), `test` (test updates), `refactor` (code restructuring), `chore` (maintenance)

## Running the Application

### Prerequisites

- **Java 26+**: Download from [Eclipse Temurin](https://adoptium.net/)
- **Node.js 24+**: Download from [nodejs.org](https://nodejs.org/)
- **Docker & Docker Compose**: Download from [docker.com](https://www.docker.com/)
- **Git**: For cloning the repository

### Cloning the Repository

```bash
git clone https://github.com/codeurjc-students/2026-TrainFyre.git
cd 2026-TrainFyre
```

### Backend Setup

1. **Start the database with Docker Compose:**

   ```bash
   cd backend/TrainFyre
   DB_PASSWORD=localdevpassword docker-compose up -d
   ```

   This starts:
   - PostgreSQL 16 on `localhost:5432`
   - Spring Boot application on `localhost:8080`
   - Sample data is loaded automatically

2. **Verify the backend is running:**

   - API root: http://localhost:8080/api/v1
   - API documentation (Swagger UI): http://localhost:8080/swagger-ui.html
   - Health check: http://localhost:8080/actuator/health

### Frontend Setup

1. **Navigate to the frontend directory:**

   ```bash
   cd frontend
   ```

2. **Install dependencies:**

   ```bash
   npm ci
   ```

3. **Start the development server:**

   ```bash
   npm run dev
   ```

   Frontend runs at: http://localhost:5173

### Building for Production

**Backend:**
```bash
cd backend/TrainFyre
mvn clean package
```

**Frontend:**
```bash
cd frontend
npm run build
```

## Testing

### Running Backend Tests

```bash
cd backend/TrainFyre

# Unit tests only (fast, no external dependencies)
mvn test -Dtest='!*IntegrationTest,!*E2ETest,!TrainFyreApplicationTests'

# All tests including integration and E2E
mvn verify

# Specific test class
mvn test -Dtest=MyTestClass

# With JaCoCo coverage report
mvn verify  # Coverage report: target/site/jacoco/index.html
```

### Running Frontend Tests

```bash
cd frontend

# Unit tests
npm run test:unit

# Unit tests with coverage
npm run test:coverage  # Coverage report: coverage/index.html

# Integration tests (requires running backend API)
npm run test:integration

# End-to-end tests (requires running backend API)
npm run test:e2e
```

For integration and E2E tests, ensure the backend is running:
```bash
cd backend/TrainFyre
DB_PASSWORD=localdevpassword docker-compose up -d
```

### Test Traceability

The following table documents which tests cover each major functionality:

| Functionality | Unit | Integration | E2E |
|---|---|---|---|
| Load all incidences from API (paging) | `useAllIncidences`, `getIncidences` | `Incidences.integration` | `incidences.spec` |
| List incidences (name, lines, delay) | `Incidences` page | `Incidences.integration` | `incidences.spec` |
| Filter by map | `Incidences` page | `Incidences.integration` | `incidences.spec` |
| Navigation (menu, logo) | `App`, `layout/*`, `Inicio` page | — | `incidences.spec` |

## API Documentation

### OpenAPI Specification

The backend automatically generates OpenAPI 3.1 documentation from Spring annotations:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Spec Files**: Located in `docs/api/`
  - `api-docs.yaml`: OpenAPI specification (YAML format)
  - `api-docs.html`: Generated HTML documentation

### API Base URL

All REST endpoints are prefixed with `/api/v1` and require appropriate authentication (JWT token for registered users and administrators).

### Example API Calls

Use Postman or similar tools. A Postman collection is available in the repository with example requests:

```bash
# List all incidences (paginated)
GET http://localhost:8080/api/v1/incidences?page=0&size=20

# Get specific incidence
GET http://localhost:8080/api/v1/incidences/{id}

# Create new incidence (admin only)
POST http://localhost:8080/api/v1/incidences
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

{
  "name": "Line 5 Delay",
  "summary": "Delays on Line 5 due to technical issues",
  "severity": "WARNING"
}
```

---

## Continuous Integration & Deployment

### GitHub Actions Workflows

The project uses GitHub Actions to automate testing and quality checks:

#### `unit-tests.yml` - Basic Quality Control

**Trigger:** Every push to any branch except `main`

**Jobs:**
- Backend unit tests (JUnit, no integration tests)
- Frontend unit tests and build
- Runs in parallel for speed

#### `full-tests.yml` - Complete Quality Assurance

**Trigger:** Pull requests to `main` and merges to `main`

**Jobs:**
- Backend: Unit + Integration + E2E tests
- Frontend: Unit + Integration + E2E tests  
- SonarQube Cloud: Static analysis and coverage reporting

**Requirements for merge:**
- All tests must pass
- Code must meet SonarQube quality gate
- Code review approval required

### Local Testing Before Push

Always run tests locally before pushing:

```bash
# Backend
cd backend/TrainFyre
mvn clean verify

# Frontend
cd frontend
npm run test:unit
npm run test:coverage
```

---

## Follow-up

GitHub Project used to track the development of the project: [TrainFyre GitHub Project](https://github.com/orgs/codeurjc-students/projects/52)

You can also visit: [Project blog]()

## Authors

This application is being developed as part of the Bachelor's Thesis of Pablo Sainz López,
who is pursuing a double degree in Computer Engineering and Computer Systems Engineering at the ETSII of the URJC,
under the supervision of Natalia Madrueño Sierro, who is the Bachelor's Thesis tutor.

## AI Tools Usage

AI tools have been used throughout the development of this project to support
research, documentation, design, and other development-related tasks.
Below is a summary of the AI usage for each phase completed or currently in progress. 

### Phase 1: Definition of features and screens

* **Google Gemini** was used to research similar transport applications and to assist with Markdown formatting.
* **OpenAI ChatGPT** was used to review and refine the project's documentation, objectives, methodology, and domain analysis.

### Phase 2: Repository, testing and CI

* AI tools are being used to document the development setup, testing strategies, and CI/CD configuration.

Full details of every specific use (date, objective, tool, version, configuration, etc.) are recorded in
[docs/AI_USAGE.md](docs/AI_USAGE.md).

## See also

- [Changelog](CHANGELOG.md)
- [Development Guide](docs/DEVELOPMENT.md)
- [API Documentation](http://localhost:8080/swagger-ui.html)

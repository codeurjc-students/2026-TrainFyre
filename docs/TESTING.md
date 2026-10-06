# Testing guide

The project includes automated tests across the backend and frontend to guarantee correctness of the main flows and consistency of the implementation.

## Backend testing

The backend uses JUnit 5, Mockito, and Testcontainers.

### Test categories

- Unit tests: validation of domain logic and service behavior
- Integration tests: persistence and repository interaction
- End-to-end tests: HTTP requests and API validation

### Current backend test libraries

- `spring-boot-starter-webmvc-test`
- `spring-boot-starter-data-jpa-test`
- `h2`
- `testcontainers-junit-jupiter`
- `testcontainers-postgresql`
- `testcontainers-mariadb`
- `rest-assured`

### Example commands

```bash
cd backend/TrainFyre
mvn test -Dtest='!*IntegrationTest,!*E2ETest,!TrainFyreApplicationTests'
mvn verify
```

## Frontend testing

The frontend uses Vitest and Playwright.

### Test categories

- Unit tests for components and hooks
- Integration tests for client-server interactions
- End-to-end tests through a browser

### Current frontend test libraries

- Vitest
- Testing Library
- jsdom
- Playwright
- V8 coverage provider

### Example commands

```bash
cd frontend
npm run test:unit
npm run test:coverage
npm run test:integration
npm run test:e2e
```

## Coverage

The backend uses JaCoCo to generate coverage reports in the Maven build. The project also supports SonarQube Cloud analysis to provide quality gate checks and coverage reporting for the main branch.

The frontend uses the V8 coverage provider for unit coverage reports.

## Traceability

The following tasks are currently covered by the test structure:

| Functionality | Unit | Integration | E2E |
| --- | --- | --- | --- |
| Load incidences | `useAllIncidences` / service layer | `Incidences.integration` | `incidences.spec` |
| List incidents | page and UI logic | integration tests | E2E UI tests |
| Filter by map | page logic | integration tests | E2E UI tests |
| Navigation | layout and app components | not applicable | E2E UI tests |

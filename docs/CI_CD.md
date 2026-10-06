# CI/CD guide

This project uses GitHub Actions to automate validation steps, ensure code quality, and prevent broken changes from reaching the `main` branch.

## Workflows

### 1. `unit-tests.yml`

This workflow is triggered on push events for all branches except `main`.

It executes:

- backend unit tests
- frontend build
- frontend unit tests

The workflow is designed to run quickly and provide early feedback during feature development.

### 2. `full-tests.yml`

This workflow is triggered on pull requests to `main` and pushes to `main`.

It executes:

- backend unit + integration + E2E tests
- frontend unit + integration + E2E tests
- JaCoCo coverage generation
- SonarQube Cloud analysis

This workflow acts as the complete quality gate before merging into the main branch.

## Resulting quality requirements

The project requires:

- all tests to pass
- no failures in the quality gate
- successful static analysis in SonarQube Cloud
- clean review and merge process for main branch updates

## Local validation before pushing

```bash
cd backend/TrainFyre
mvn verify

cd ../...
cd frontend
npm run test:unit
npm run test:coverage
```

## API readiness check

The CI workflow includes an automatic check to wait for the backend API to be ready before running integration and E2E tests:

```bash
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:8080/incidence?page=0&size=1" > /dev/null; then
    echo "API ready"; exit 0
  fi
  echo "Waiting for the API ($i/60)..."; sleep 3
done
```

This ensures the API is fully operational before proceeding with client-server integration tests.

## Deployment logic

The application is currently prepared for Docker-based local execution and will evolve to more complete deployment configurations as the project advances.

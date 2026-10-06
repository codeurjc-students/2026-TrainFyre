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

cd ../..
cd frontend
npm run test:unit
npm run test:coverage
```

## Deployment logic

The application is currently prepared for Docker-based local execution and will evolve to more complete deployment configurations as the project advances.

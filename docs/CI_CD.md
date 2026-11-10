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

### 3. `publish.yml`

This workflow publishes the Docker artifacts required by Phase 3:

- pushes to `main` publish the application image and development Compose as `dev`;
- published GitHub Releases publish the versioned image, `latest`, and the stable
  Compose OCI artifact;
- `workflow_dispatch` accepts a branch or tag and an optional commit SHA, and
  publishes an identifiable branch/date/commit build.

The workflow uses `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`. With the current
configuration, the image repository is `escipion48/trainfyre` and the Compose
OCI artifacts use the separate `escipion48/trainfyre-compose` repository because
one registry tag cannot refer to both an image manifest and a Compose artifact.
The latter repository must exist in the DockerHub account before the first
publication.

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
  if curl -fsS --cacert "$NODE_EXTRA_CA_CERTS" "https://localhost:443/api/v1/incidence?page=0&size=1" > /dev/null; then
    echo "API ready"; exit 0
  fi
  echo "Waiting for the API ($i/60)..."; sleep 3
done
```

This ensures the API is fully operational before proceeding with client-server integration tests.

## Deployment logic

The application is currently prepared for Docker-based local execution. The
Dockerfile and Compose file are kept in `docker/`. Compose uses the
repository root as its build context and mounts runtime secrets from
`backend/TrainFyre/secrets`. When running locally, provide `docker/.env`
explicitly:

Development execution uses `docker/docker-compose-dev.yml`; the published
version uses `docker/docker-compose.yml` and pulls the image from DockerHub:

```bash
docker compose --env-file docker/.env \
  -f docker/docker-compose-dev.yml up -d --build

DOCKERHUB_USERNAME=escipion48 docker compose \
  --env-file docker/.env \
  -f docker/docker-compose.yml up -d
```

Copy `docker/.env.example` to `docker/.env` and replace every placeholder
before starting the application. The `.env` file and
`backend/TrainFyre/secrets/` must never be committed: they contain passwords,
private keys and certificates. A deployment machine must provision those files
through its secret-management mechanism.

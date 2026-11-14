# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Entries are ordered chronologically, with the newest version first.

## [Unreleased]

Changes included in version 0.1.0, the first release of the project (Phase 3: basic functionality and Docker). Replace this heading with `## [0.1.0] - <release date>` when the release is published.

### Added

- Users: registration, sign in with JWT access tokens (RS256, 15 minutes), reading, updating and deleting the own account, and alert subscriptions by line, time window and days of the week.
- Roles `USER` and `ADMIN`, with Spring Security as a stateless OAuth2 resource server and Argon2 password hashing.
- Avatars: upload, download and deletion of the profile picture, stored in an S3-compatible service (AWS SDK; Garage in development).
- Incidences: public paginated list and administrator operations to create, read, update and delete them.
- REST API documented with OpenAPI (`docs/api/api-docs.yaml` and `docs/api/api-docs.html`) and a Postman collection with an example request for every operation.
- Web client: home, registration and sign in, incidence list with filters and pagination, incidence detail and editor, account page with avatar and subscriptions, user administration page, and not-found and error pages.
- Sample data for the `dev` profile: 300 incidences and the accounts `admin`, `dev_alice` and `dev_bob`.
- HTTPS on port 443, with the compiled client served by the backend (`SpaController`).
- Docker packaging: `docker/Dockerfile`, `docker/docker-compose.yml` (published version) and `docker/docker-compose-dev.yml` (development version with PostgreSQL and Garage), configured with environment variables.
- Continuous delivery workflow `publish.yml`: `dev` image and Compose artifact on every push to `main`, versioned image, `latest` and Compose artifact on every GitHub release, and manual builds from any branch or commit.
- Release check that the tag, `pom.xml` and `package.json` versions match.
- Architecture verification with Spring Modulith and jMolecules.
- Tests for the new functionality: server unit, controller, integration and system tests, client unit, component and integration tests, Playwright scenarios, and Gatling load simulations.
- Quality control: JaCoCo coverage for the server, Vitest coverage for the client and a single SonarQube Cloud analysis, executed in the complete quality workflow.
- `TRAINFYRE_SECRETS_DIR` variable to choose the folder of the TLS keystore and JWT keys in the Compose files, and `GARAGE_RPC_SECRET` variable instead of a secret stored in `garage.toml`.
- Continuous integration workflows: `unit-tests.yml` (basic control on feature branches) and `full-tests.yml` (complete control on pull requests to `main`). The complete workflow starts the stack with `docker/docker-compose-dev.yml` and uses ephemeral TLS and JWT credentials.
- Performance: the list of incidences is read with slices and the total count is cached.
- Documentation: features, detailed functionalities, execution, architecture, development guide, quality control, CI/CD and releases, and the AI usage log.

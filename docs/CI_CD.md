# CI/CD and releases

TrainFyre uses GitHub Actions for continuous integration (quality controls) and continuous delivery (publication of Docker images and Compose files in DockerHub). The workflows are in `.github/workflows/`.

| Workflow | File | Trigger | Purpose |
| --- | --- | --- | --- |
| Unit tests | `unit-tests.yml` | Push to any branch except `main` | Basic quality control |
| Full test suite + SonarQube Cloud | `full-tests.yml` | Pull request to `main` and push to `main` | Complete quality control |
| Publish Docker artifacts | `publish.yml` | Push to `main`, GitHub release published, manual dispatch | Continuous delivery |

## Continuous integration

### Basic quality control (`unit-tests.yml`)

Runs on every push to a feature or fix branch. A new push to the same branch cancels the previous run.

- **Backend unit tests** (JDK 27): `mvn test` excluding the tests that start the Spring context or use Docker (`*IntegrationTest`, `*E2ETest`, `TrainFyreApplicationTests`). What remains are the domain, command, service and controller tests with mocks, and `ArchitectureTest`.
- **Frontend build and unit tests** (Node.js 24): `npm ci`, `npm run build` (type checking and production build) and `npm run test:unit`.

### Complete quality control (`full-tests.yml`)

Runs when a pull request to `main` is opened or updated (draft pull requests are skipped) and when a change is pushed to `main`. It has three jobs:

1. **Backend**: `mvn verify`, which runs every server test (unit, controller, integration with Testcontainers and system tests with Rest Assured) and generates the JaCoCo report.
2. **Frontend**: builds the client, runs the unit, component and integration tests with coverage, generates ephemeral TLS and JWT credentials, starts the backend, the database and Garage with `docker/docker-compose-dev.yml` (with fixed throwaway values for the `DB_PASSWORD`, `TRAINFYRE_TLS_PASSWORD` and `GARAGE_*` variables), waits until the REST API answers (up to 60 attempts), runs the client - API integration tests and the Playwright tests, and stops the containers.
3. **SonarQube Cloud analysis**: runs after the two previous jobs and sends a single analysis with the server and client coverage.

To prevent merging when a quality control fails, the jobs of this workflow have to be marked as *required status checks* in the branch protection rules of `main` (Settings > Branches). The workflow cannot enforce this by itself.

The static analysis is part of the complete quality control and not of the basic one.

## Continuous delivery (`publish.yml`)

One workflow and one job publish everything, so that the tag calculation, the build and the publication logic are not duplicated. Two kinds of artifacts are published to DockerHub:

- the **application image**, in the repository `<DOCKERHUB_USERNAME>/trainfyre`;
- the **Docker Compose file** as an OCI artifact (media type `application/vnd.docker.compose.v1+yaml`, pushed with ORAS), in the repository `<DOCKERHUB_USERNAME>/trainfyre-compose`. A separate repository is needed because one registry tag cannot refer to an image manifest and to a Compose artifact at the same time. It must exist in the DockerHub account before the first publication.

| Event | Image tag | Compose artifact tag | Compose file published |
| --- | --- | --- | --- |
| Push to `main` (merge of a branch) | `dev` | `dev` | `docker-compose-dev.yml` |
| GitHub release published | The version of the release (`0.1` becomes `0.1.0`; `0.1.0` stays `0.1.0`) plus `latest` | The tag of the release plus `latest` | `docker-compose.yml` with the image tag replaced by the version |
| Manual dispatch (`workflow_dispatch`) with a branch or tag and an optional commit | `<branch>-<UTC date and time>-<commit>` | The same | `docker-compose-dev.yml` |

The workflow needs the repository secrets `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`.

The image is built with `docker/Dockerfile`:

1. A Node.js stage compiles the React application.
2. A Maven stage copies the compiled client into `src/main/resources/static`, compiles the server and prepares the Spring AOT output (`-Pnative`; the AOT profile is `dev` in local builds and `prod` in the published image).
3. The runtime stage is `eclipse-temurin:27-jre`, runs with a non-root user (`10001`) and exposes port 443.

On a release, the workflow verifies before publishing that the version of the tag, the `version` in `backend/TrainFyre/pom.xml` and the `version` in `frontend/package.json` are the same, and fails otherwise.

## Versioning and releases

Versions follow `MAJOR.MINOR.PATCH`. A release is published as a GitHub release whose tag is the version.

| Moment | `backend/TrainFyre/pom.xml` | `frontend/package.json` | `docker/docker-compose.yml` and `docker/.env.example` |
| --- | --- | --- | --- |
| Just before the release | `0.1.0` | `0.1.0` | image tag `0.1.0` |
| Just after the release | `0.2.0-SNAPSHOT` | `0.2.0` | image tag `0.2.0` |

Steps to create a release:

1. Merge the feature branches into `main` through pull requests with the complete quality control passing.
2. In a commit on `main`, set the versions of the table (just before the release), including the `CHANGELOG.md` entry.
3. Create a GitHub release with the tag of the version. The `publish.yml` workflow builds and publishes the image (`0.1.0` and `latest`) and the Compose artifact.
4. In a new commit on `main`, set the versions for the next development version (just after the release).
5. Check in DockerHub that the tags exist.

To try the process before the first real release, publish test releases with versions such as `0.0.1` and `0.0.2`, and delete them (release, tag and DockerHub tags) when the process works.

### Releases

| Version | Date | Contents |
| --- | --- | --- |
| 0.1.0 | _pending_ | Version 0.1: registration and sign in, user accounts, avatars, alert subscriptions and public and administrative management of incidences. See [`CHANGELOG.md`](../CHANGELOG.md) |

<!-- TODO(author) PENDING IMAGES: after merging to main and publishing 0.1.0, save as docs/images/23-actions.png (successful runs of the workflows) and docs/images/25-dockerhub.png (repository escipion48/trainfyre with the 0.1.0 tag), and replace this comment with:
     ![GitHub Actions runs](images/23-actions.png)
     ![Image published in DockerHub](images/25-dockerhub.png) -->

## Running the published version

The instructions to run the application are in [Execution](EXECUTION.md). The Compose files mount the TLS keystore and the JWT keys from the folder in `TRAINFYRE_SECRETS_DIR`, which by default is `../backend/TrainFyre/secrets/` (relative to the Compose file, so inside a clone of the repository). To run a Compose file downloaded from DockerHub outside a clone, set `TRAINFYRE_SECRETS_DIR` to the folder where the secrets were created.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

TrainFyre: React 19 + TypeScript SPA served by a Spring Boot 4.1 / Java 27 modular monolith, with PostgreSQL 18 and S3-compatible storage (Garage in dev). It is a Bachelor's Thesis project: the author must understand and own the backend code (see Workflow). For depth, read `docs/` (`ARCHITECTURE.md`, `TESTING.md`, `CI_CD.md`, `API.md`) instead of re-deriving it.

## Commands

Backend (`cd backend/TrainFyre`, use the wrapper; on Windows `mvnw.cmd`). JDK 27 required; Docker is needed for the Testcontainers tests.

```bash
./mvnw test -Dtest='!*IntegrationTest,!*E2ETest,!TrainFyreApplicationTests' -Dsurefire.failIfNoSpecifiedTests=false   # unit tests, no Docker (same as CI)
./mvnw verify                                  # everything + JaCoCo (target/site/jacoco)
./mvnw test -Dtest=IncidenceTest               # single class (use Class#method for one method)
```

Frontend (`cd frontend`, Node 24): `npm ci`, `npm run dev` (Vite on :5173), `npm run build` (tsc + vite), `npm run typecheck`, `npm run lint` (oxlint), `npm run test:unit | test:components | test:integration | test:coverage | test:e2e` (Playwright needs `npx playwright install chromium`). Single test: `npx vitest run tests/unit/<file>`.

Full stack with sample data: `docker compose --env-file docker/.env -f docker/docker-compose-dev.yml up -d --build` (needs `docker/.env` from `.env.example`, certificate and JWT keys; see `docs/EXECUTION.md`). Serves https://localhost; rebuild the image after backend changes. Open it once in the browser to accept the self-signed certificate before using the Vite client.

## Backend architecture

Package root `es.codeurjc.students.trainfyre`. Modules: `users`, `statistics` (incidences), `images` (avatars), `security`, `common`. Each module is hexagonal: `domain` → `application/{port/in, port/out, service}` → `infrastructure/adapter/{in/web, out/...}`.

- Controllers call input ports (use cases with command/query objects, CQRS style) and never touch repositories. Services implement use cases and depend on output ports (`UserPort`, `IncidencePort`, `ImagePort`). Domain must not depend on Spring/JPA (only jMolecules and Lombok).
- Spring Modulith's `ArchitectureTest` (`ApplicationModules.verify()`) fails the build on cross-module violations. Types shared across modules must be exposed deliberately (e.g. `security` uses a named interface).
- Authorization: route rules in `SecurityConfiguration`; ownership rules (user owns account, avatar owner or ADMIN) live in the application layer (`UserAccessPolicy`, ownership checks in commands), not in controllers.
- Stateless JWT (RS256, 15 min), Argon2id passwords, HTTPS with PKCS12 keystore. API under `/api/v1`; the OpenAPI spec and Postman collection in `docs/api/` must match the controllers.
- Maps and lines are not entities: an `Incidence` references them by numeric id. An `INFORMATIONAL` incidence must have zero duration.

## Frontend architecture

Layers in `frontend/src`: `pages` → `components` / `hooks` → `api` → `model`. No router, no HTTP library, no UI library: routes come from the URL hash (`Routes.tsx`), requests use `fetch` via `api/http.ts`, styles are plain CSS (`App.css`). `Require` only hides UI; the backend enforces authorization. Incident lists load all pages and filter/paginate in the browser. Keep these constraints unless asked to change them.

## Conventions

- Code, comments, commits and docs in English; user-facing UI text in Spanish.
- Commit format (the author commits, not Claude): `type(scope): short description`, with type `feat|fix|test|refactor|docs` and scope the affected layer/module. Branches are `<issue>-<slug>`; PRs go to `main` (GitHub Flow).
- Test conventions (naming, parameterization, TestBuilders, test type per layer) live in the `/tests` skill (`.claude/skills/tests`). Backend test suffixes matter: CI separates `*IntegrationTest` and `*E2ETest` (Docker) from unit tests.
- Sonar quality gate (SonarQube Cloud `codeurjc-students_2026-TrainFyre`, runs on PRs to `main`): new code needs A ratings, coverage >= 80%, duplication <= 3%, hotspots reviewed. Internal targets: method complexity < 15, ~30 lines, max 5 parameters; coverage domain 95%, application 90%, infrastructure 80%, controllers 70%.
- Before finishing a task, check the `sonarqube` MCP (`get_project_quality_gate_status`, `search_sonar_issues_in_projects`, with `pullRequest` or `branch`) and fix new issues in code you touched. It needs Docker running (`/mcp` reconnects).

## Workflow

**Backend (TDD, the author implements):** the author writes the class/signature; Claude writes only the red tests via `/tests`, from rules the author gives (ask, never guess rules); the author commits, implements until green; refactors happen together on request. Never write backend production code unless explicitly asked.

**Frontend (more autonomy):** Claude may create, extend and refactor from `docs/api` within the architecture above, using modern maintainable practices. Every change needs tests (Vitest; Playwright if a flow changes) and must pass lint, typecheck and Sonar.

**Docs:** the author updates `docs/`, `CHANGELOG.md` and the OpenAPI/Postman files in bulk at the end of each delivery, so do not edit them as you go unless asked. Do not add entries to `docs/AI_USAGE.md` except when explicitly requested.

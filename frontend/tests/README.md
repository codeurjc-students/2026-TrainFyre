# Frontend tests

| Type | Folder | Tool | Needs backend? | Command |
|---|---|---|---|---|
| Unit (component, hook, service) | `tests/unit` | Vitest + Testing Library + jsdom | No (axios and hooks are mocked) | `npm run test:unit` |
| Unit + coverage | `tests/unit` | Vitest + V8 | No | `npm run test:coverage` |
| Client-server integration | `tests/integration` | Vitest + Testing Library | **Yes** (real REST API) | `npm run test:integration` |
| System / E2E (UI) | `e2e` | Playwright | **Yes** (real REST API) | `npm run test:e2e` |

## Running the tests that need the backend

```bash
docker compose -f backend/TrainFyre/docker-compose.yaml up -d --build   # API on :8080 with example data
cd frontend
npm run test:integration
npx playwright install chromium    # first time only
npm run test:e2e                   # starts the Vite dev server on :5173 by itself
```

The backend only allows CORS from `http://localhost:5173`, so the integration tests make jsdom
use that origin and the E2E tests run the frontend on that port.

## Coverage

`npm run test:coverage` writes the report to `frontend/coverage/` (open `coverage/index.html`).
It measures `src/**` and `components/layout/**`; `components/ui/**` (shadcn generated code) and
`src/types/**` (types only) are excluded. Thresholds are set to 100% in `vitest.config.ts`.

## Traceability (functionality -> tests)

| Functionality | Unit | Integration | System (E2E) |
|---|---|---|---|
| Load all the incidences from the API (paging) | `hooks/useAllIncidences`, `services/getIncidences` | `Incidences.integration` | `incidences.spec` (example data) |
| List incidences (name, lines, delay) | `pages/Incidences` | `Incidences.integration` | `incidences.spec` |
| Filter by map | `pages/Incidences` | `Incidences.integration` | `incidences.spec` |
| Navigation (menu, logo) | `App`, `layout/*`, `pages/Inicio` | - | `incidences.spec` |

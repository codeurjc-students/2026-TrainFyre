# Development guide

## Introduction

TrainFyre is a web application with a SPA frontend and a REST backend. The application is organized into three main layers:

- Frontend: React + TypeScript + Vite
- Backend: Java 26 + Spring Boot + JPA + OpenAPI
- Database: PostgreSQL

This architecture allows the application to be modularized and scaled progressively while maintaining a clear separation of concerns.

## Summary of the project structure

```text
.
├── .github/
│   └── workflows/
│       ├── unit-tests.yml
│       └── full-tests.yml
├── backend/
│   └── TrainFyre/
│       ├── Dockerfile
│       ├── docker-compose.yaml
│       ├── pom.xml
│       └── src/
├── frontend/
│   ├── package.json
│   ├── src/
│   └── tests/
├── README.md
├── CHANGELOG.md
├── docs/
└── .gitignore
```

## Technologies used

### Backend

- Java 26
- Spring Boot 4.1.1
- Spring Modulith
- jMolecules
- Spring Data JPA
- PostgreSQL driver
- Springdoc OpenAPI
- Testcontainers
- JaCoCo

### Frontend

- React 19
- TypeScript
- Vite
- Tailwind CSS
- Recharts
- Axios
- Vitest
- Playwright

## Environment setup

### Prerequisites

- Java 26+
- Node.js 24+
- Docker and Docker Compose
- Git

### Backend execution

```bash
cd backend/TrainFyre
DB_PASSWORD=localdevpassword docker-compose up -d
```

This starts the PostgreSQL database and the backend service. The application is then exposed on port 8080.

### Frontend execution

```bash
cd frontend
npm ci
npm run dev
```

The frontend is normally available at:

- http://localhost:5173

## Quality controls

The project includes two levels of CI quality control:

1. Basic control: triggered on feature branch pushes
2. Full control: triggered on PRs to `main` and on pushes to `main`

## Development workflow

The project follows GitHub Flow:

- `main` is protected and stable
- feature branches are created per task
- pull requests are reviewed before merge
- workflows validate changes before merging

## Important project commands

### Backend

```bash
cd backend/TrainFyre
mvn test
mvn verify
```

### Frontend

```bash
cd frontend
npm run build
npm run test:unit
npm run test:integration
npm run test:e2e
```

## Documentation and code organization

The main documentation is split into the `docs/` directory to keep the repository organized and aligned with the Phase 2 requirements.

The most relevant documentation files are:

- [OBJECTIVES.md](OBJECTIVES.md)
- [METHODOLOGY.md](METHODOLOGY.md)
- [FUNCTIONALITIES.md](FUNCTIONALITIES.md)
- [ANALYSIS.md](ANALYSIS.md)
- [TESTING.md](TESTING.md)
- [CI_CD.md](CI_CD.md)
- [API.md](API.md)

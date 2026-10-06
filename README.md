# TrainFyre: A web application for real-time transport alert management

TrainFyre is a full-stack application for monitoring, classifying, and presenting transport incidents in a clear and useful way. The system is designed as a modular monolith with a Spring Boot backend, a React SPA frontend, and a PostgreSQL persistence layer, with Docker support for local execution and CI/CD automation through GitHub Actions.

> [!IMPORTANT]
> This project has already started its implementation phase. The functional and technical objectives were defined in Phase 1, and the development setup, test strategy, and CI automation are being completed in Phase 2.

## Documentation index

- [Objectives](docs/OBJECTIVES.md)
- [Methodology](docs/METHODOLOGY.md)
- [Detailed functionalities](docs/FUNCTIONALITIES.md)
- [Analysis](docs/ANALYSIS.md)
- [Project tracking](docs/PROJECT_TRACKING.md)
- [Authors](docs/AUTHORS.md)
- [Development guide](docs/DEVELOPMENT.md)
- [Testing guide](docs/TESTING.md)
- [CI/CD guide](docs/CI_CD.md)
- [API documentation](docs/API.md)
- [AI usage log](docs/AI_USAGE.md)

## Phase plan

| Phase | Description | Date |
| --- | --- | --- |
| Phase 1 | Definition of functionalities and screens | 15 September |
| Phase 2 | Repository, testing and CI | 15 October |
| Phase 3 | Version 0.1 - Basic functionality and Docker | 15 December |
| Phase 4 | Version 0.2 - Intermediate functionality | 1 March |
| Phase 5 | Version 1.0 - Advanced functionality | 15 April |
| Phase 6 | Memory | 15 May |
| Phase 7 | Defense | 15 June |

## Project overview

TrainFyre collects and processes alerts from different transport-related sources in order to offer a service similar to that provided by official transit information portals and mobility apps.

The current architecture follows these principles:

- Backend in Java 26 + Spring Boot 4.1.1
- Frontend in React 19 + TypeScript + Vite
- PostgreSQL persistence
- Dockerized development environment
- GitHub Actions for CI + SonarQube Cloud quality checks
- Hexagonal architecture + DDD + Spring Modulith modularization

## Functional summary

### Basic functionality

- User registration and login
- Public transport line status views
- Alert subscriptions for registered users
- Profile management and upload of user images

### Intermediate functionality

- Statistics about incidents by line and map
- Subscription management for alerts of interest
- Detailed incident listing and filtering
- Administration of system entities

### Advanced functionality

- Advanced alerts and notifications
- Event-driven integration expansions
- Additional cloud-based services or AI-powered enhancements
- Scalability and observability improvements

## Screens and navigation

The initial interface prototype has been defined in Figma:

- [TrainFyre mockups](https://www.figma.com/design/4TL76NTkyb7FL6PTQH29iN/Trainfyre-maquetaci%C3%B3n?node-id=305-5749&t=TcFFmUE4bhh7lFds-1)

The prototype covers the main screens for the initial version of the project and may evolve as the implementation progresses.

## Follow-up

- GitHub Project: [TrainFyre GitHub Project](https://github.com/orgs/codeurjc-students/projects/52)
- Blog / news: not defined yet

## Authors

This application is being developed as part of the Bachelor's Thesis of Pablo Sainz López, student of the Computer Engineering and Computer Systems Engineering degree at ETSII, URJC, under the supervision of Natalia Madrueño Sierro.

## Development guide

The detailed development guide is available in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). It includes architecture, local setup, testing commands, and technical tooling used in the project.

## Changelog

The project changelog is available in [CHANGELOG.md](CHANGELOG.md).

## AI usage

The detailed AI usage log is available in [docs/AI_USAGE.md](docs/AI_USAGE.md).

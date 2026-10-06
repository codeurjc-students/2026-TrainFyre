# Methodology

The project follows an iterative and incremental development methodology. The application is developed in phases, each one with a concrete deliverable and an explicit validation milestone.

## Development phases

| Phase | Description | Deadline |
| --- | --- | --- |
| Phase 1 | Definition of functionalities and screens | 15 September |
| Phase 2 | Repository, testing and CI | 15 October |
| Phase 3 | Version 0.1 - Basic functionality and Docker | 15 December |
| Phase 4 | Version 0.2 - Intermediate functionality | 1 March |
| Phase 5 | Version 1.0 - Advanced functionality | 15 April |
| Phase 6 | Memory | 15 May |
| Phase 7 | Defense | 15 June |

## Working model

The methodology is based on the following principles:

- Requirements are refined progressively as development advances
- Each iteration increases the implemented functionality
- Quality control is included from the early phases of the project
- Testing is considered a core part of development, not a final step
- CI/CD checks must be green before code is merged to the main branch

## Architectural methodology

The current implementation uses a monolith-first strategy with modular boundaries to allow future service decomposition if needed.

The main design approaches are:

- Hexagonal architecture: separation of domain logic from infrastructure and external systems
- Domain-Driven Design: clear domain concepts such as `User`, `Map`, `Line`, and `Incidence`
- Event-driven thinking: preparation for future decoupled communication patterns
- Test-driven evolution: automated tests accompany implementation and verification

## Collaboration workflow

The repository follows GitHub Flow:

- `main` contains the production-ready state
- Feature branches are created for each functional development task
- Changes are merged through pull requests
- Quality gates run automatically with GitHub Actions

## Project management

The project uses GitHub Issues and a GitHub Project board to manage tasks.

Current tracking link:

- [TrainFyre GitHub Project](https://github.com/orgs/codeurjc-students/projects/52)

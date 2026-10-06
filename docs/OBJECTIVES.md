# Objectives

The project goal is to build a web application for monitoring and presenting transport alerts and line-related status information, similar to public transport information systems and mobility applications.

## Functional objectives

The functional objectives are organized by user role and are introduced progressively during the project.

### Anonymous user
- Register in the application
- View public transport status and general system statistics
- Search for line-related information without requiring authentication

### Registered user
- Log in and log out
- Manage a personal profile and profile image
- Create and maintain alert subscriptions for lines of interest
- Receive notifications related to the relevant alerts

### Administrator
- Manage main application entities such as maps, lines, and incidents
- Access system-level audit and monitoring information
- Maintain the data needed for the service operation and quality control

## Technical objectives

TrainFyre is designed as a maintainable and extensible monolithic application with a strong focus on modularization, testability, and domain-driven design.

The project includes the following technical objectives:

- Implement a backend with Java 26 and Spring Boot 4.1.1
- Expose a REST API with OpenAPI documentation
- Use a React + TypeScript frontend built with Vite
- Persist data with PostgreSQL
- Apply Hexagonal Architecture and Domain-Driven Design principles
- Use Spring Modulith to enforce modular boundaries
- Implement automated tests at unit, integration, and end-to-end levels
- Add CI/CD workflows with GitHub Actions
- Use SonarQube Cloud for code quality analysis
- Build a Dockerized environment for reproducible local and deployment setups

## Scope and current status

The project has defined its objectives, architecture direction, and initial domain model. The implementation is already underway in the backend and frontend, although the full system is not yet complete.

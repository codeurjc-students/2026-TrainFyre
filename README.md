# TrainFyre: A web application for real-time transport alert management

## Description

A system capable of collecting data from various transport sources. 
This data will be used and processed to provide various functionalities, 
such as allowing users to receive notifications regarding lines of interest
and statistics related to those lines.

[TODO example screens]

## Objectives

The objectives of this project center on the ability to collect and process
alerts from various sources in order to offer a range of services to users similar
to those provided by official websites like [Metro Madrid](https://www.metromadrid.es/es) 
and [Renfe](https://www.adif.es/viajeros/estado-de-la-red), or apps like [Moovit](https://moovitapp.com/es).

> [!WARNING]
> Only the objectives will be defined below; however, their implementation has not yet begun.

### Functional objectives

The functional objectives will be delivered in a phased manner across iterations.
Some functional objectives may change in the future.

These functional objectives/features will be accessible based on user roles:
anonymous, registered, and administrator. These are listed in order of privilege,
with administrators having access to all available features.

The project's functional objectives are listed below, indicating the minimum
privilege level (role) required for access:

* Register in the system (anonymous).
* View general statistics for the lines or their status (anonymous).
* Create alert subscriptions to subsequently receive notifications (registered).
* Log in and log out (registered).
* CRUD operations on entities other than the user (admin).
* View or collect audit information and statistics on the system and/or lines (admin).

As the project progresses and it becomes apparent which features could offer more value, 
they will be added to this list.

### Technical objectives

The technical objectives focus on building a maintainable and extensible monolithic application capable of collecting,
processing, and managing transport alerts from multiple sources.
The system will follow established software engineering principles and architectural patterns to ensure low coupling, testability, and maintainability.

* **Modular monolithic architecture based on Hexagonal Architecture (Ports and Adapters)**, separating the domain logic from infrastructure and external technologies.
* **Domain-Driven Design (DDD) and Event-Driven Architecture (EDA)** principles to model the transport domain and decouple application components following a monolith-first approach.
* **Integration of multiple data sources** through dedicated adapters, together with the ingestion, normalization, and processing of heterogeneous data into a consistent internal representation.
* **Full-stack web application**, with a **Java and Spring Boot** backend exposing a RESTful API and a **React** frontend implemented as a Single-Page Application (SPA).
* **Authentication and authorization** using **JWT tokens and role-based access control**, with different permissions for anonymous, registered, and administrator users.
* **Alert subscription and notification management**, allowing registered users to subscribe to alerts related to selected transport lines and receive notifications by email.
* **Persistence and management of application data**, including the information required by the application and its audit capabilities.
* **Automated testing following TDD principles**, promoting maintainability and reliability throughout development.
* **Static code analysis and software quality control** using **SonarQube**.
* **Containerization using Docker** to provide reproducible development and deployment environments.

## Methodology

Work will be carried out in iterations, meeting objectives in a phased manner
that allows for changes. Similarly, only dates for key milestones will be scheduled,
and work completed for each delivery (one delivery per milestone) will be recorded,
capturing the start and end dates of tasks performed during the iteration.

Given the complexity and difficulty of the project's objectives,
sound software design principles, patterns, and practices will be followed
to the extent possible; these are:

* Hexagonal architecture: It is useful, as it will receive data from various sources.
  Furthermore, its modularity will facilitate testing and maintenance.
* DDD (domain driven design): improving communication and maintainability
  (ubiquitous language), enabling the creation of a more modular and maintainable system
* EDA (Event driven architecture): Enabling the reduction of dependencies between
  services and their subsequent division into microservices (monolith-first approach).
* TDD (test driven development): improving software design,
  drastically reducing errors, and enabling code modification without fear,
  thanks to a safety net of automated tests created before the source code

A Gantt chart will be added at the end of the project to properly showcase the 
phases, for the moment the project has the following table:

| Phase   | Description                                  | Deadline     |
| ------- | -------------------------------------------- | ------------ |
| Phase 1 | Definition of functionalities and screens    | 15 September |
| Phase 2 | Repository, testing and CI                   | 15 October   |
| Phase 3 | Version 0.1 - Basic functionality and Docker | 15 November  |
| Phase 4 | Version 0.2 - Intermediate functionality     | 15 December  |
| Phase 5 | Version 1.0 - Advanced functionality         | 15 January   |
| Phase 6 | Report                                       | 15 May       |
| Phase 7 | Defense                                      | 15 June      |


The Gantt chart will be added [HERE] when it is created.
You can see the current project status on its related GitHub project on: "Follow-up"


## Detailed features

The following table summarizes the main functionalities planned for TrainFyre and the minimum user role required to access them.

|                  | Anonymous                                                               | Registered                                                      | Admin                                                            |
| ---------------- | ----------------------------------------------------------------------- | --------------------------------------------------------------- | ---------------------------------------------------------------- |
| **Basic**        | Register in the system                                                  | Log in and log out and upload and update his profile pictures   | Manage system entities through CRUD operations                   |
| **Intermediate** | View the current status of transport lines and general alert statistics | Create and manage alert subscriptions and receive notifications | View system and transport alert statistics and audit information |
| **Advanced**     | TODO                                                                    | TODO                                                            | TODO                                                             |

> [!NOTE]
> The advanced functionalities are not yet defined and will be specified as the project progresses. The features listed in the table may also be refined or extended during development.


## Analysis

This section documents the current analysis and design decisions made for the project.
As TrainFyre is being developed following an iterative and incremental methodology,
the level of detail in this section will evolve as the project progresses.

The current documentation focuses on the aspects that have already been sufficiently analyzed to support the initial development.
Some design decisions, particularly those related to advanced functionality and implementation-specific details,
are intentionally left open until the corresponding requirements and technical constraints are better understood.
This approach aims to avoid making premature design decisions that could unnecessarily constrain the evolution of the system.

### Screens and navigation

The initial user interface has been designed as a prototype using Figma:

[Website layout](https://www.figma.com/design/4TL76NTkyb7FL6PTQH29iN/Trainfyre-maquetaci%C3%B3n?node-id=305-5749&t=TcFFmUE4bhh7lFds-1)

The prototype defines the main navigation structure and the screens required for the currently identified basic and intermediate functionality.
It should be considered an initial design rather than a final specification,
as some screens and navigation flows may change during implementation as usability and technical requirements are validated.

More detailed descriptions of each screen and its navigation flow will be added as the corresponding functionality is refined and implemented.
Advanced functionality is intentionally not fully represented at this stage,
as some of these features may depend on technologies or services that have not yet been evaluated.

### Entities

The current domain model is centered around transport lines, maps, and users. Following Domain-Driven Design (DDD) principles,
the model distinguishes between entities, value objects, and domain or integration events according to their responsibilities within the domain.

The main entities currently identified are:

* **User**, representing a registered user of the application. `User` is modeled as an aggregate root and contains the user's alert subscription information as part of its state.
* **Line**, representing a transport line managed by the application.
* **Map**, representing a collection of transport lines. `Map` is modeled as an aggregate root and maintains its associated lines as part of the aggregate.

The current model also includes concepts that are deliberately not modeled as entities:

* **Alert subscription** is modeled as a **Value Object** associated with the `User` aggregate, as it represents subscription information rather than an independently identifiable entity.
* **Alert** and **Notification** are modeled as **domain or integration events**, representing relevant occurrences that can trigger or communicate changes within the system rather than persistent domain entities.

The domain model is still subject to refinement as the requirements and implementation progress.
Additional entities or concepts may be identified if required by the evolving domain or by functionalities introduced in later iterations.
The final attributes, relationships, aggregate boundaries, and persistence details will be documented once they have been sufficiently validated.

To better showcase the design here we have some UMLS:

Classes UML: [TODO]

Component Diagram: [TODO]

### User permissions

The application defines three user roles:

* **Anonymous users** can access public information, including transport line status and general alert-related statistics, and can register an account.
* **Registered users** can authenticate, manage their alert subscriptions, and receive notifications related to the transport lines they are interested in. They also can upload and edit ther profile pictures.
* **Administrators** have access to management operations over application entities, as well as complete system and audit information access.

The authorization model will follow the principle of least privilege,
ensuring that users can only perform operations allowed by their role and, where applicable, on data they own.

The detailed authorization rules for individual resources will be documented as the corresponding entities and operations are implemented.

### Images

The application is required to support image uploads.
The specific domain entities associated with images have not yet been finalized.

However at the moment the class User is confirmed that will have an image related to each User entity,
as shown in the previous uml, basically a profile picture.

### Charts

Charts will be used to represent statistical information related to transport lines and alerts.

The current analysis considers possible visualizations such as:

* The number of alerts associated with a transport line over a given time period.
* The distribution of alerts across different transport lines.
* Aggregated information about alerts that can help users understand the current or historical status of the transport network.

The exact charts and visualization types will be refined once the available alert data and the statistics provided by the application have been defined in more detail.
This avoids committing to visualizations that may not be supported by the final data model.

### Complementary technology

Email notifications have been identified as the first complementary technology for the application.

Registered users will be able to subscribe to transport lines and receive notifications when relevant alerts are detected for those lines.
This functionality provides a direct use case for the collected alert data and complements the main web interface.

Other complementary technologies may be considered during later iterations if they provide a clear benefit to the application and fit within the project's scope.

### Algorithm or advanced query

The application will include advanced data processing or querying beyond basic CRUD operations.
The current direction is to provide useful analysis over the collected transport alerts, such as filtering and aggregating alerts by transport line and time period.

There also may be an advance query to find all affected users by an Alert.

The exact algorithm or advanced query has not yet been finalized because it depends on the final structure and characteristics of the collected alert data.
The selected solution will be documented once the data model and the requirements for the corresponding functionality have been sufficiently defined.

This section will be updated with the final algorithm or query, its purpose, and the way it is integrated into the application.


## Follow-up

GitHub Project used to track the development of the project: [TrainFyre GitHub Project](https://github.com/orgs/codeurjc-students/projects/52)

You can also visit: [Project blog]()

## Authors

This application is being developed as part of the Bachelor's Thesis of Pablo Sainz López,
who is pursuing a double degree in Computer Engineering and Computer Systems Engineering at the ETSII of the URJC,
under the supervision of Natalia Madrueño Sierro, who is the Bachelor's Thesis tutor.

## AI Tools Usage

AI tools have been used throughout the development of this project to support
research, documentation, design, and other development-related tasks.
Below is a summary of the AI usage for each phase completed or currently in progress. 

### Phase 1: Definition of features and screens

* **Google Gemini** was used to research similar transport applications and to assist with Markdown formatting during the preparation of this README.

* **OpenAI ChatGPT (GPT-5.6 Luna)** was used to review and refine the project's documentation, objectives, methodology, and domain analysis.


Full details of every specific use
(date, objective, tool, version, configuration, etc.) are recorded in
[AI_USAGE.md](https://github.com/codeurjc-students/2026-PabloSainz/blob/main/AI_USAGE.md).

## See also

[Changelog](https://github.com/codeurjc-students/2026-PabloSainz/blob/main/CHANGELOG.md)

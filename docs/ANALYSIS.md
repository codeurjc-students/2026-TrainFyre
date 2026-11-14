# Analysis

This document summarizes the current analysis and design decisions for TrainFyre.

## Screen design and navigation

The initial prototype was created in Figma and defines the main navigation structure of the app.

- [Prototype](https://www.figma.com/design/4TL76NTkyb7FL6PTQH29iN/Trainfyre-maquetaci%C3%B3n?node-id=305-5749&t=TcFFmUE4bhh7lFds-1)

The prototype currently covers the early screens needed for the basic and intermediate functionality, such as:

- Home screen
- Line status dashboard
- Incidences list view
- Filter and search view
- User profile and subscription management

## Main entities

The domain model currently focuses on the transport domain and includes the following concepts:

- `User`: registered user of the platform
- `Map`: transport map or network representation
- `Line`: transport line managed and displayed by the system
- `Incidence`: event or alert affecting one or more lines

In version 0.1 the backend implements `User` (module `users`) and `Incidence` (module `statistics`) as domain aggregates with value objects and persistence adapters. `Map` and `Line` are not entities yet: an incidence references them through numeric identifiers. See [Architecture](ARCHITECTURE.md) for the implemented domain model.

## Permissions and roles

The system defines three user roles:

- Anonymous: public consultation and registration
- Registered: authenticated access, subscriptions, and personal data management
- Administrator: management access for domain entities and analytics

The authorization model is expected to follow the principle of least privilege and restrict access to only the operations each role is allowed to perform.

## Images

The application supports image uploads. The use case implemented in version 0.1 is the user profile image (avatar), stored in an S3-compatible object storage.

## Charts and statistical visualization

The system plans to render statistic charts (not implemented in version 0.1) related to:

- number of incidents per transport line
- distribution of incidents by time window
- aggregate information about alerts affecting a map or line

## Complementary technology

The first complementary technology selected is email notification integration for registered users. Users can subscribe to specific lines and receive alerts associated with them. In version 0.1 the subscriptions are stored, but no e-mail is sent yet.

## Advanced algorithm or query

The application will include advanced processing beyond CRUD operations, especially for incident analysis. Examples include:

- incidence aggregation by line and time period
- filtering of incidents by map or affected network
- advanced identification of impacted users or subscribers for a given alert

This logic is still being refined with the final domain model and is not implemented in version 0.1.

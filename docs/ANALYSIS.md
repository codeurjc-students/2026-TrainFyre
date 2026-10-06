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

The current implementation in the backend reflects this domain, especially in the `statistics` module where `Incidence` is modeled as a domain aggregate with value objects and persistence adapters.

## Permissions and roles

The system defines three user roles:

- Anonymous: public consultation and registration
- Registered: authenticated access, subscriptions, and personal data management
- Administrator: management access for domain entities and analytics

The authorization model is expected to follow the principle of least privilege and restrict access to only the operations each role is allowed to perform.

## Images

The application supports image uploads. At the moment, the main confirmed use case is the user profile image.

## Charts and statistical visualization

The system plans to render statistic charts related to:

- number of incidents per transport line
- distribution of incidents by time window
- aggregate information about alerts affecting a map or line

The frontend already includes `recharts` as a dependency for chart rendering.

## Complementary technology

The first complementary technology selected is email notification integration for registered users. Users can subscribe to specific lines and receive alerts associated with them.

## Advanced algorithm or query

The application will include advanced processing beyond CRUD operations, especially for incident analysis. Examples include:

- incidence aggregation by line and time period
- filtering of incidents by map or affected network
- advanced identification of impacted users or subscribers for a given alert

This logic is still being refined with the final domain model.

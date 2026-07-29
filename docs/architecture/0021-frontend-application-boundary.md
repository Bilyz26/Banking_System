# ADR 0021: Separate frontend application boundary

## Status

Accepted

## Context

The repository contains a Spring Boot banking API and an early static dashboard
prototype. The prototype simulates financial data and successful operations,
has no authentication, and cannot provide the type safety, testing, routing,
or state management required by the frontend product requirements.

The frontend must evolve independently while continuing to use the
version-controlled REST contract and the existing Keycloak deployment.

## Decision

Build the production frontend as a separate React and TypeScript application in
`frontend/`.

Use:

- Vite for development and production bundling;
- TanStack Router for type-safe client-side routes;
- TanStack Query for API server state;
- React Hook Form and Zod for forms and boundary validation;
- Vitest and Testing Library for automated tests; and
- package-by-feature organization under `src/features`.

Shared technical capabilities live under `src/shared`. Application composition
and cross-cutting providers live under `src/app`. Features must not import from
another feature's internal modules; shared contracts are promoted deliberately.

The Spring Boot API and frontend remain separately buildable and deployable.
The existing static prototype is not a production source and will be removed
when the application shell replaces it.

## Consequences

- Frontend dependencies and failures do not alter the Java build lifecycle.
- The browser application can be tested and released independently.
- Docker and CI must later coordinate two build systems.
- Cross-origin development and production routing require explicit
  configuration.
- Generated or handwritten API contracts must continue to match the reviewed
  OpenAPI document.

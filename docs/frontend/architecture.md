# Frontend Architecture

## Architectural style

The frontend is a package-by-feature modular application. The browser is an
untrusted presentation client: it assists the user, but the Java application
remains authoritative for validation, authorization, balances, ledger entries,
and transaction atomicity.

```text
Application composition
        |
        +--> Feature packages
        |       |
        |       +--> Shared API contracts and transport
        |       +--> Shared design system
        |
        +--> Authentication and runtime configuration
```

Dependencies point from concrete features toward stable shared boundaries.
Features do not import other features. This prevents page-to-page coupling and
keeps each banking capability independently testable.

## Important decisions

### TanStack Router

Routes are declared centrally because navigation is application composition,
while every route component stays inside its feature. Pages use dynamic imports
to preserve feature boundaries in the delivered JavaScript.

### TanStack Query

Remote data is server state, not global UI state. Query keys, cache invalidation,
loading state, and retry behavior remain explicit at the network boundary.

### Runtime API validation

TypeScript types disappear after compilation. Zod contracts therefore validate
untrusted HTTP responses at runtime before a feature renders them. A contract
mismatch becomes a controlled application error instead of corrupted UI state.

### OAuth 2.0 with PKCE

The browser uses a public OIDC client and Authorization Code with PKCE. Tokens
are limited to the current browser session. Route protection improves the user
experience, but backend scope enforcement remains the security boundary.

### Design system

Small composable primitives centralize accessible focus behavior, semantic
status communication, visual tokens, confirmation behavior, and reduced-motion
support. Feature packages compose these primitives instead of inheriting from
page base classes.

## Adding a feature

1. Create a package under `src/features/<feature>`.
2. Define runtime contracts at the shared API boundary only when the server
   contract introduces new data.
3. Keep transport calls out of visual primitives.
4. Add focused unit or component tests and an accessibility assertion.
5. Add a lazy route in `Application.tsx`.
6. Run `npm run verify` and `npm run test:e2e`.

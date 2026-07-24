# ADR 0019: Secured local Compose deployment

## Status

Accepted

## Context

The existing Compose environment started PostgreSQL and the application, but
security was optional and required an authorization server outside the stack.
There was no reproducible way to obtain a compatible token or verify a secured
banking workflow. Default credentials and ports also needed clearer local-only
boundaries.

## Decision

The local Compose environment starts three services:

- PostgreSQL 17 for durable banking data;
- Keycloak 26.7.0 as a development OpenID Connect provider;
- the banking application with the `postgresql,secure` profiles active.

Keycloak imports a version-controlled `banking` realm containing a public
command-line client, the four API scopes, and one local developer user. The
token issuer remains the browser-visible localhost URL. The application reads
the signing keys from Keycloak's internal Compose address, allowing issuer
validation and container networking to coexist.

All published ports bind to `127.0.0.1`. Compose waits for PostgreSQL and
Keycloak health checks before starting the application. Development values are
documented in `.env.example`; `.env` is ignored. The application continues to
run as a non-root user with a read-only filesystem and no-new-privileges.

## Consequences

- A fresh checkout can run and verify a secured local environment without an
  external identity provider.
- Local tokens exercise the same issuer, signature, and scope validation used
  by deployed environments.
- The imported user and passwords are intentionally public development
  fixtures and must never be reused outside a developer machine.
- Keycloak `start-dev` and password grants are accepted only for local
  verification; production deployments require TLS, managed identities, and
  an approved OAuth flow.
- Changing the public Keycloak port requires updating both
  `KEYCLOAK_PUBLIC_URL` and `BANKING_JWT_ISSUER_URI` consistently.

# ADR 0020: Production security boundaries

## Status

Accepted

## Context

Security controls that depend on shared state or deployment topology cannot be
implemented reliably inside a single application instance. Examples include
distributed rate limiting, TLS certificate management, network policy,
credential rotation, and database backup scheduling.

## Decision

The application owns authorization, input validation, financial invariants,
idempotency, transaction integrity, safe errors, security headers, and
security-relevant tests.

The trusted deployment platform owns:

- TLS termination and certificate renewal;
- per-principal and per-source rate limiting;
- maximum request-body size and upstream timeouts;
- secret storage and rotation;
- private network policy;
- encrypted PostgreSQL backups and restore drills;
- immutable log retention and alert routing.

Production activates the `postgresql`, `secure`, and `production` profiles.
The production profile enables graceful shutdown, forwarded-header handling,
and bounded database connection validation.

Security automation rejects high or critical dependency, repository, and image findings.
The Trivy action is pinned to the verified commit behind signed release
`v0.36.0`, rather than a mutable tag.

## Consequences

- application instances remain stateless and horizontally scalable;
- rate limits cannot diverge between instances;
- local Compose remains a development environment, not a production template;
- deployment owners must complete the environment-specific checklist before
  processing real customer data or funds;
- third-party action updates require explicit review and commit repinning.

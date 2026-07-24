# ADR 0018: Production-like integration testing

## Status

Accepted

## Context

The persistence integration tests used H2 in PostgreSQL compatibility mode.
That approach was fast, but it could not verify PostgreSQL SQL semantics,
driver behavior, Flyway compatibility, or transactional behavior against the
database used in production. The API integration tests also exercised either
security or the full banking journey, but not both together with PostgreSQL.

## Decision

Integration tests that verify JDBC persistence and transaction boundaries run
against a disposable PostgreSQL 17 container managed by Testcontainers. A
separate end-to-end test starts the complete application with the
`postgresql` and `secure` profiles and exercises the REST API using JWT claims
and scopes.

The container tests are disabled when Docker is unavailable. Continuous
integration is the authoritative environment for these tests because its
runner provides Docker. Faster domain, application, and web tests remain
container-independent.

## Consequences

- Flyway migrations and JDBC statements are verified against real PostgreSQL.
- Optimistic locking, idempotency, and rollback tests now use production
  database semantics.
- A secured customer-to-transfer-to-history workflow crosses every application
  boundary.
- Developers need Docker to execute the complete integration suite locally.
- The CI build takes longer because it must pull and start PostgreSQL.

# ADR 0007: PostgreSQL persistence with explicit JDBC

## Status

Accepted

## Context

The original delivery roadmap named PostgreSQL, JPA, and Flyway as the Phase 8
persistence stack. During implementation, the project used Spring JDBC instead
of JPA. The persistence approach must be explicit so that contributors do not
introduce a parallel ORM model or assume the difference is unfinished work.

## Decision

The production persistence profile uses PostgreSQL, Flyway migrations, Spring
JDBC repositories, and explicit transaction committers.

Spring JDBC is the accepted replacement for the roadmap's planned JPA
integration. Phase 8 is considered complete with this documented variance. JPA
must not be added alongside JDBC unless a later ADR demonstrates a concrete need
and includes a migration plan.

Domain classes remain free of persistence annotations. The `postgresql` Spring
profile replaces all in-memory repository and commit adapters as one set.

Accounts carry an optimistic version. Updates include the expected version in
their `WHERE` clause and fail when another operation has already changed the
account.

## Rationale

Explicit SQL keeps persistence behavior visible and avoids coupling the domain to
an ORM. Transaction boundaries remain behind output ports, preserving the
application layer's independence from Spring.

Optimistic locking prevents lost balance updates without holding database locks
while business rules execute.

Replacing the working adapters with JPA solely to match the original roadmap
would duplicate tested persistence behavior, obscure critical balance-update
SQL, and add migration risk without improving a current business requirement.

## Consequences

- Flyway is the only production schema-management mechanism.
- JDBC repositories are the single production persistence implementation.
- Persistence models remain separate from the domain rather than becoming JPA
  entities.
- Deposits, withdrawals, and transfers update accounts and append ledger records
  in one database transaction.
- Concurrent updates fail with a retriable conflict instead of silently
  overwriting a newer balance.
- The default profile remains in-memory for quick local development.

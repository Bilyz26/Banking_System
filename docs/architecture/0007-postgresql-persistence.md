# ADR 0007: PostgreSQL persistence with explicit JDBC

## Status

Accepted

## Decision

The production persistence profile uses PostgreSQL, Flyway migrations, Spring
JDBC repositories, and explicit transaction committers.

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

## Consequences

- Flyway is the only production schema-management mechanism.
- Deposits, withdrawals, and transfers update accounts and append ledger records
  in one database transaction.
- Concurrent updates fail with a retriable conflict instead of silently
  overwriting a newer balance.
- The default profile remains in-memory for quick local development.

# ADR 0005: Commit account state and ledger facts through one boundary

- Status: Accepted
- Date: 2026-07-23

## Context

A deposit or withdrawal changes an account balance and appends a ledger entry.
Persisting only one of those changes would make the balance and financial history
inconsistent.

## Decision

- Coordinate deposits and withdrawals in an application service.
- Generate ledger identifiers and timestamps through injected dependencies.
- Submit the changed account and immutable ledger entry to one
  `AccountOperationCommitter` port.
- Implement the temporary adapter with serialized in-memory commits.
- Return snapshots from the temporary account repository so uncommitted aggregate
  mutations cannot alter stored state.
- Implement the future PostgreSQL adapter with a real database transaction.

## Consequences

- The application service does not depend on a specific transaction technology.
- Failed domain validation produces no account or ledger commit.
- Every completed deposit and withdrawal has a correlated ledger fact.
- Durable atomicity, concurrency control, and rollback remain responsibilities of
  the PostgreSQL implementation.

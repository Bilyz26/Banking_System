# ADR 0008: Idempotent money operations

## Status

Accepted

## Context

HTTP clients retry requests when responses are lost or time out. Repeating a
deposit, withdrawal, or transfer as a new operation would move money twice.

## Decision

Require callers to supply a UUID in the `Idempotency-Key` header for every
money-changing REST request. Carry that value through the application command
and use it as the immutable ledger transaction identifier.

Before changing balances, the application service queries the ledger for that
transaction identifier:

- no entries means the operation may proceed;
- matching entries return the original result;
- entries describing different operation data produce a conflict.

The database enforces one ledger entry per transaction and entry type. This
allows one entry for a deposit or withdrawal and the debit/credit pair required
for a transfer.

## Consequences

- completed retries do not change balances twice;
- the ledger remains the source of truth for financial operation identity;
- behavior is identical for in-memory and PostgreSQL adapters;
- clients must retain the key until the operation outcome is known;
- simultaneous first attempts can still produce a transient conflict, but the
  database constraint and account optimistic lock prevent duplicate posting,
  and a subsequent retry returns the committed result.

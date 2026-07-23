# ADR 0004: Model the financial ledger as immutable posted facts

- Status: Accepted
- Date: 2026-07-23

## Context

An account balance shows current state but does not explain how that state was
reached. Banking operations require an auditable sequence of financial facts.
Mutable transaction records make reconciliation and incident investigation
unreliable.

## Decision

- Record every completed balance change as an immutable `LedgerEntry`.
- Give every entry a unique identifier.
- Correlate entries from one operation with a shared ledger transaction ID.
- Record the movement amount and resulting account balance.
- Classify entries as deposit, withdrawal, transfer debit, or transfer credit.
- Model transfers as a debit/credit pair with the same transaction ID, amount,
  and occurrence time.
- Treat every stored ledger entry as posted and final.
- Represent future corrections with compensating entries instead of editing or
  deleting existing facts.
- Keep failed attempts in an operational audit log, not the financial ledger.
- Supply identifiers and timestamps from the application layer so tests remain
  deterministic.

## Consequences

- Account history can be reconstructed and reconciled.
- Transfers can be traced across both participating accounts.
- Persistence must append ledger entries atomically with balance changes.
- Ledger storage must prohibit updates and deletions through normal application
  workflows.
- Reversals and idempotency will require explicit application use cases and
  persistence constraints in later phases.

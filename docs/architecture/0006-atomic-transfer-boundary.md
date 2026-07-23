# ADR 0006: Atomic transfer boundary

## Status

Accepted

## Decision

A transfer application service coordinates two account aggregates and creates a
paired debit and credit ledger record. Both account snapshots and both ledger
entries are passed to one `TransferCommitter` output port.

The ledger repository supports atomic batch append. Its contract requires either
all entries in a batch to be stored or none of them.

## Rationale

A transfer is one business operation even though it changes four persisted
records. Exposing a single commit port makes that consistency requirement
explicit without placing database transaction APIs in the application layer.

The temporary in-memory adapter validates and appends the ledger batch before
publishing both account snapshots. A PostgreSQL adapter will implement the same
port with one database transaction.

## Consequences

- Transfer debit and credit entries always share one transaction identifier.
- Failed validation does not publish account or ledger changes.
- Infrastructure adapters must provide an atomic implementation of the commit
  contract.

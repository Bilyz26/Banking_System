# ADR 0002: Protect banking rules inside the domain model

- Status: Accepted
- Date: 2026-07-23

## Context

Monetary arithmetic, account lifecycle changes, and transfers must remain valid
regardless of whether they are invoked from REST, a scheduled process, or another
adapter.

## Decision

- Represent money with immutable `BigDecimal` amounts and ISO currencies.
- Reject amounts with more than two decimal places instead of rounding silently.
- Give each customer and account an immutable UUID-based identity.
- Allow account balance changes only through behavior on `BankAccount`.
- Prevent overdrafts and cross-currency account operations.
- Model transfers as a domain service because their rules span two account
  aggregates.
- Validate both sides of a transfer before changing either in-memory balance.

## Consequences

- Business rules are independent of Spring and persistence.
- Invalid account state cannot be created through public field mutation.
- Persistence adapters must reconstruct accounts through the validated restore
  operation.
- Database transactions and concurrency control are still required when
  persistence is introduced.
- Supporting currencies with non-two-decimal minor units would require replacing
  the current fixed-scale policy.

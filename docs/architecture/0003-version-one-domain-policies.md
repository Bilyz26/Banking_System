# ADR 0003: Define version-one account and currency policies

- Status: Accepted
- Date: 2026-07-23

## Context

Account behavior and monetary precision must be explicit before ledger and
persistence design begins. Otherwise, infrastructure can accidentally define
business behavior.

## Decision

- Each account has exactly one ISO currency.
- Monetary amounts use the currency's ISO minor-unit precision.
- Monetary values are never rounded implicitly.
- Accounts cannot be overdrawn.
- Active accounts can receive deposits and make withdrawals.
- Frozen accounts can receive deposits but cannot make withdrawals.
- Closed accounts cannot receive deposits or make withdrawals.
- An account can close only with a zero balance.
- Transfers require different source and destination accounts in the same
  currency.
- Customer names and email addresses are limited to 200 and 320 characters,
  respectively, in both the domain and REST boundary.

## Consequences

- Multi-currency accounts and currency conversion are outside version one.
- Overdraft products require an explicit future policy rather than weakening the
  current aggregate.
- A frozen account can still receive refunds or incoming transfers.
- Persistence schemas must use sufficient precision for all supported currencies.

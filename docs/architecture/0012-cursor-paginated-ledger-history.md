# ADR 0012: Cursor-paginated ledger history

## Status

Accepted

## Context

Account transaction history can grow without bound. Returning every ledger
entry would create unpredictable database, memory, and network costs. Offset
pagination also becomes slower at large offsets and can skip or repeat entries
when newer transactions arrive between requests.

## Decision

Expose account history in newest-first order with keyset pagination:

- use `(occurred_at, ledger_entry_id)` as the deterministic descending sort key;
- request one entry beyond the bounded page size to detect another page;
- represent the last returned sort key as an opaque URL-safe cursor;
- encode and decode cursors only in the HTTP adapter;
- keep the application-layer page position independent of input and output
  ports;
- return `404 ACCOUNT_NOT_FOUND` before querying history for an unknown account.

The existing account/time/ledger-entry database index supports the query and
can be scanned in descending order, so this decision requires no migration.

## Consequences

- page cost stays bounded as an account's history grows;
- equal timestamps have a UUID tie-breaker and therefore remain stable;
- transactions inserted after page one do not shift later page boundaries;
- clients must treat cursors as opaque and restart pagination if a cursor is
  rejected;
- the cursor is not encrypted and must contain only non-sensitive positioning
  data;
- filtering by date, type, or amount can be added later with filter values
  bound into a versioned cursor format.

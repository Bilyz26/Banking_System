# Architecture Overview

Banking System is a modular monolith organized by business feature. It keeps
transactional consistency simple for version 1.0 while preserving boundaries
that can support later extraction.

```text
HTTP and security
       |
presentation adapters
       |
application input ports and services
       |
domain model and policies
       |
application output ports
       |
JDBC, audit, time, and identifier adapters
```

## Feature packages

- `customer`: customer identity and profile lifecycle;
- `account`: ownership, balances, status, deposits, and withdrawals;
- `transfer`: atomic movement between accounts;
- `ledger`: immutable financial history and pagination;
- `shared`: money, application errors, and presentation infrastructure;
- `bootstrap`: dependency composition and runtime profiles.

The domain has no Spring, HTTP, JDBC, or serialization dependency. Application
services depend on ports. Infrastructure implements output ports, and
controllers call input ports.

## Consistency model

PostgreSQL transactions commit account state and ledger entries together.
Optimistic account versions reject stale writes. Each transfer creates paired
debit and credit entries under one transaction identifier. Idempotency keys
make client retries safe and reject conflicting reuse.

## Runtime profiles

- default: in-memory adapters and development security;
- `postgresql`: JDBC repositories, Flyway, and transaction boundaries;
- `secure`: OAuth 2.0 resource-server and scope authorization;
- `production`: proxy awareness, graceful shutdown, and bounded database waits.

Production activates `postgresql,secure,production`. Architecture decisions
and their consequences are recorded as numbered ADRs in `docs/architecture`.

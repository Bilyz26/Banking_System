# Banking System

A production-oriented banking REST API built incrementally with Java, Spring Boot,
Clean Architecture, and package-by-feature organization.

## Architecture

The system begins as a modular monolith. Each business feature owns its domain,
application, infrastructure, and presentation concerns.

Dependencies point inward:

```text
presentation ─┐
              ├──> application ──> domain
infrastructure┘
```

The domain must not depend on Spring, HTTP, or persistence technologies.

Detailed architectural decisions are recorded in
[`docs/architecture/0001-modular-monolith.md`](docs/architecture/0001-modular-monolith.md).

## Requirements

- Java 21
- Maven 3.6.3 or newer

## Running locally

```shell
mvn spring-boot:run
```

The application has no banking endpoints yet. Features will be added in small,
reviewable steps.

## Current implementation

Part 1, the framework-independent banking domain, includes:

- immutable monetary values with currency-safe arithmetic
- immutable customer identities and validated customer details
- account ownership, balances, deposits, withdrawals, and lifecycle rules
- overdraft prevention
- currency-safe transfers between accounts
- focused unit tests for domain rules and failure cases

Persistence, application use cases, and REST endpoints intentionally remain for
later parts.
# Banking_System

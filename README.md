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

`JAVA_HOME` must point to the Java 21 installation, or Java must be available on
the system `PATH`.

## Running locally

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```shell
./mvnw spring-boot:run
```

The Maven Wrapper downloads the project-pinned Maven version automatically, so a
global Maven installation is not required.

## Verifying the project

On Windows:

```powershell
.\mvnw.cmd clean verify
```

On Linux or macOS:

```shell
./mvnw clean verify
```

The application currently exposes customer creation at
`POST /api/v1/customers`. Additional features will be added in small, reviewable
steps.

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

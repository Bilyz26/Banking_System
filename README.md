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

## Running with PostgreSQL

Start PostgreSQL:

```shell
docker compose up -d postgres
```

Run the application with durable persistence:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgresql"
```

The profile reads `BANKING_DB_URL`, `BANKING_DB_USERNAME`,
`BANKING_DB_PASSWORD`, and `BANKING_DB_POOL_SIZE`. The defaults match
[`compose.yml`](compose.yml). Flyway applies pending migrations at startup.

## Running with API security

Set `BANKING_JWT_ISSUER_URI` to the issuer identifier of your OAuth 2.0
authorization server, then activate the `secure` profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=secure"
```

For durable production-style operation, activate both profiles:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgresql,secure"
```

The default profile intentionally permits unauthenticated requests for local
development. Do not expose it to an untrusted network. The `secure` profile
validates bearer JWTs and enforces the scopes documented in
[`docs/api.md`](docs/api.md).

## Verifying the project

On Windows:

```powershell
.\mvnw.cmd clean verify
```

On Linux or macOS:

```shell
./mvnw clean verify
```

The complete endpoint reference is available in
[`docs/api.md`](docs/api.md).

## Current implementation

The current modular-monolith implementation includes:

- immutable monetary values with currency-safe arithmetic
- customer creation and in-memory customer storage
- account opening, retrieval, and lifecycle management
- account ownership, balances, deposits, withdrawals, and lifecycle rules
- overdraft prevention
- atomic transfer application boundaries with paired ledger records
- retry-safe deposits, withdrawals, and transfers using idempotency keys
- optional OAuth 2.0 JWT authentication with scope-based authorization
- REST endpoints with request validation and stable error responses
- unit and end-to-end MockMvc tests

The default profile uses in-memory adapters for quick local work. The
`postgresql` profile provides durable JDBC repositories, Flyway migrations,
optimistic account locking, and database transaction boundaries.

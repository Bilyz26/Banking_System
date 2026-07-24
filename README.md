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
The authoritative delivery phases and their current status are recorded in
[`docs/roadmap.md`](docs/roadmap.md).

## Requirements

- Java 21
- Docker Desktop or Docker Engine with Compose

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

Copy the local environment template:

```powershell
Copy-Item .env.example .env
```

Start the secured local environment:

```shell
docker compose up --build --detach
```

Compose starts:

- the banking API at `http://localhost:8080`;
- PostgreSQL at `localhost:5432`;
- Keycloak at `http://localhost:9000`.

The application starts with PostgreSQL persistence and JWT security enabled.
Compose waits for the database and identity provider to become healthy before
starting it. All published ports bind only to `127.0.0.1`.

Verify readiness, token issuance, authorization, persistence, and a deposit:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\verify-local-deployment.ps1
```

The expected result contains `Result: PASS`. The script creates a disposable
customer and account in the local database. The imported Keycloak development
user is `banking-developer` with password `banking-local-change-me`.

Inspect or stop the environment:

```shell
docker compose ps
docker compose logs --follow application
docker compose down
```

Use `docker compose down --volumes` only when you intentionally want to delete
all local banking data and recreate the environment. The values in
`.env.example` and the imported Keycloak user are public development fixtures,
not production secrets.

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

## Observability

Operational endpoints are available under `/actuator`:

- `/actuator/health`, `/actuator/health/liveness`, and
  `/actuator/health/readiness`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

With the `secure` profile, health probes remain public while info, metrics, and
Prometheus require `banking.monitor`. Every HTTP response contains an
`X-Correlation-ID`; callers may supply a safe value or let the application
generate one. The same value is included in application log lines.

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
With the application running, interactive API documentation is available at
`http://localhost:8080/swagger-ui.html`.

The `verify` lifecycle also enforces Clean Architecture boundaries, the Java and
Maven toolchain, source-quality checks, bytecode defect analysis, and a minimum
line-coverage baseline. Reports are generated under `target/site`.

## Continuous integration

The GitHub Actions workflow in [`.github/workflows/ci.yml`](.github/workflows/ci.yml)
runs the Maven `verify` lifecycle with Java 21, uploads the executable JAR, and
builds the production container image. Pull requests must pass this workflow
before they are merged.

## Current implementation

The current modular-monolith implementation includes:

- immutable monetary values with currency-safe arithmetic
- customer creation, retrieval, profile updates, and in-memory customer storage
- account opening, retrieval, and lifecycle management
- account ownership, balances, deposits, withdrawals, and lifecycle rules
- overdraft prevention
- atomic transfer application boundaries with paired ledger records
- retry-safe deposits, withdrawals, and transfers using idempotency keys
- cursor-paginated, newest-first account transaction history
- a version-controlled OpenAPI 3.1 contract with runtime Swagger UI and route, DTO, type, enum, and validation-constraint drift verification
- optional OAuth 2.0 JWT authentication with scope-based authorization
- health probes, Prometheus metrics, and request correlation IDs
- reproducible CI verification and non-root container packaging
- REST endpoints with request validation and stable error responses
- unit and end-to-end MockMvc tests

The default profile uses in-memory adapters for quick local work. The
`postgresql` profile provides durable JDBC repositories, Flyway migrations,
optimistic account locking, and database transaction boundaries.

# Contributing

Thank you for improving Banking System. Contributions should preserve the
project's financial correctness, Clean Architecture boundaries, and public API
compatibility.

## Before opening a change

1. Search existing issues and pull requests.
2. Open an issue before large features or architecture changes.
3. Never include credentials, access tokens, customer data, or production logs.
4. Review [SECURITY.md](SECURITY.md) for private vulnerability reporting.

## Development workflow

Requirements:

- Java 21;
- Docker with Compose;
- Git.

Create a focused branch, make one coherent change, and run:

```shell
./mvnw clean verify
docker compose config --quiet
```

On Windows, use `mvnw.cmd`. Tests requiring PostgreSQL use Testcontainers and
therefore require a running Docker engine.

## Design expectations

- Business rules remain framework-independent.
- Dependencies point from presentation and infrastructure toward application
  ports and domain objects.
- Prefer immutable values and explicit application boundaries.
- Financial state changes require atomic persistence and immutable ledger
  records.
- Validate input at the HTTP boundary and enforce invariants in the domain.
- Add tests that demonstrate both the success case and important failures.
- Record significant or irreversible decisions as an ADR under
  `docs/architecture`.
- Update the OpenAPI contract and conformance tests for API changes.

## Pull requests

Keep pull requests small enough to review. Explain what changed, why, risks,
compatibility impact, and verification evidence. All CI and security checks
must pass. By contributing, you agree that your contribution is licensed under
the Apache License 2.0.

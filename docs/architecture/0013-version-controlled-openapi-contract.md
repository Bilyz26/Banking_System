# ADR 0013: Version-controlled OpenAPI contract

## Status

Accepted

## Context

Human-readable API notes are useful but cannot reliably drive client
generation, gateway configuration, or automated compatibility checks. Runtime
documentation generation alone also hides contract changes until the
application is built and started.

## Decision

Maintain an OpenAPI 3.1 JSON document in `docs/openapi`.

An architecture integration test compares every registered `/api/v1` Spring
route with the method/path pairs in the contract. The same test requires a
unique, non-blank operation ID for every operation.

The contract describes bearer authentication and records the required
application scope through `x-required-scope`. This extension is used because
the authorization server and OAuth flow are deployment concerns, while the API
is responsible for JWT bearer-token validation.

The application packages this exact reviewed file and serves it at
`/openapi/banking-api.json`. Swagger UI loads that resource rather than a
separately generated contract. Runtime OpenAPI generation is disabled to
preserve one source of truth.

## Consequences

- client tooling can consume the contract without running the service;
- pull requests clearly show API contract changes;
- undocumented or obsolete routes fail the build;
- developers can explore the reviewed contract through `/swagger-ui.html`;
- the packaged and repository contracts are checked for exact structural
  equality;
- schema details still require deliberate review because route comparison
  cannot prove that every JSON field matches its Java DTO;
- CI publishes the contract as a build artifact.

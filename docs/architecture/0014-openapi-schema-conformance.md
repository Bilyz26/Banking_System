# ADR 0014: OpenAPI schema conformance and publication

## Status

Accepted

## Context

Route-level contract checks detect missing operations but cannot detect a Java
request or response field that was renamed, added, or removed without updating
its OpenAPI schema. Developers and automated tools also need a verified
contract artifact tied to a specific commit.

## Decision

Maintain an explicit mapping between REST boundary records and OpenAPI
component schemas in the architecture test.

For each mapped record, compare its Java record-component names with the
schema's property names. Resolve composed `allOf` schemas so reusable money
operation fields remain supported without duplicating schemas.

After Maven verification succeeds, upload the contract as a separate GitHub
Actions artifact named with the commit SHA. Retain it for seven days, matching
the executable JAR retention policy.

## Consequences

- DTO field drift fails the build;
- composed request schemas remain reusable;
- each CI run produces an immutable, commit-specific API contract;
- mappings are deliberately explicit and must be extended for new REST DTOs;
- field-name comparison does not yet prove compatible data types, formats, or
  validation constraints;
- release workflows can later publish long-lived, version-tagged contracts.

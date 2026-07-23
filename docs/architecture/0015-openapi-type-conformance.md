# ADR 0015: OpenAPI type and enum conformance

## Status

Accepted

## Context

Matching DTO and schema property names does not prevent an incompatible type,
format, collection item, or enum change. Such drift can compile successfully
while breaking generated clients at runtime.

## Decision

Extend the OpenAPI architecture test to compare each mapped record component
with its resolved schema:

- UUID values use `string` with the `uuid` format;
- instants use `string` with the `date-time` format;
- decimal values use `number`;
- integer values use `integer`;
- lists use `array` and their item type is checked recursively;
- maps and nested records use `object`;
- Java enums use `string` and must exactly match documented enum values.

Resolve component references and composed schemas before applying these rules.
Keep all checks in test code so production behavior and dependencies remain
unchanged.

## Consequences

- incompatible field-type and enum drift fails CI;
- list element contracts are protected in addition to container types;
- the comparison remains deterministic and requires no runtime documentation
  generator;
- supported REST boundary types are intentionally explicit;
- validation annotations such as size and numeric bounds still require a
  separate conformance rule.

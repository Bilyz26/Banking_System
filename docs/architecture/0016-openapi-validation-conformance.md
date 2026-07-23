# ADR 0016: OpenAPI request-validation conformance

## Status

Accepted

## Context

Type-safe schemas can still promise validation rules that differ from the
server. A generated client may accept a value that the API rejects, or reject
a value the API accepts, when required fields, length limits, patterns, or
numeric bounds drift.

## Decision

Compare request-record Jakarta validation annotations with their resolved
OpenAPI schemas:

- `@NotNull` and `@NotBlank` define required properties;
- `@NotBlank` requires a minimum string length;
- `@Size` defines string length limits;
- `@Pattern` defines the regular-expression constraint;
- `@Email` requires the email format;
- `@DecimalMin` defines inclusive or exclusive numeric minimums;
- `@Digits` defines decimal precision and an exclusive power-of-ten upper
  bound.

Normalize explicit start/end anchors when comparing regular expressions,
because Java validation matches the complete value while JSON Schema regular
expressions search unless anchored.

Keep request DTO-to-schema mappings explicit and resolve `$ref` and `allOf`
before comparison.

## Consequences

- request validation drift fails CI;
- generated clients receive more accurate input constraints;
- the contract now documents the full accepted money range without a
  precision-sensitive large fractional JSON literal;
- custom validators and cross-field business rules remain outside this
  mechanical conformance check;
- new supported validation annotations must be added deliberately.

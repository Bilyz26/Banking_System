# ADR 0017: Automated architecture and quality gates

## Status

Accepted

## Context

Clean Architecture boundaries and code-quality expectations were documented but
depended on manual review. As the project grows, accidental dependencies,
environment drift, untested code, and common implementation defects need to
fail the same build used locally and in continuous integration.

## Decision

Run the following gates through Maven's `verify` lifecycle:

- ArchUnit tests enforce domain and application independence, adapter
  separation, and controller placement.
- Maven Enforcer requires Java 21, Maven 3.9 or newer, convergent dependencies,
  and unique dependency declarations.
- Checkstyle applies a focused project-owned baseline to production and test
  sources.
- SpotBugs performs maximum-effort bytecode analysis and fails on medium or
  higher confidence defects.
- JaCoCo publishes line coverage and requires at least 90 percent project-wide
  line coverage.

Quality suppressions must be narrow, documented, and justified by a false
positive or unavoidable generated behavior. Lowering a gate requires a
superseding ADR.

## Consequences

- architectural drift and common defect patterns fail before merge;
- contributors use one `mvn verify` command for all quality checks;
- coverage trends have an enforceable floor without treating coverage as a
  substitute for meaningful assertions;
- builds take longer because bytecode and coverage analysis run during
  verification;
- the baseline can be raised incrementally as the project matures.

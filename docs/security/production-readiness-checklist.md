# Production Readiness Checklist

Version 1.0 is release-ready when the repository checks below pass. A real
production deployment must additionally complete the operator-owned controls.

## Repository-owned controls

- [x] Java 21 and Maven versions are enforced.
- [x] Unit, architecture, integration, secured E2E, and PostgreSQL tests pass.
- [x] Line coverage remains at or above 90%.
- [x] Checkstyle and SpotBugs report no violations.
- [x] The complete dependency graph is scanned for high or critical findings.
- [x] Repository configuration and secrets are scanned.
- [x] The built application image is scanned for high and critical findings.
- [x] Third-party security scanning is pinned to a verified immutable commit.
- [x] JWT authentication uses issuer, signature, lifetime, and scope checks.
- [x] Security responses avoid token details and include defensive HTTP headers.
- [x] Money operations are transactional, idempotent, and ledger-backed.
- [x] Containers run non-root with a read-only application filesystem.
- [x] Graceful shutdown and bounded database connection waits are configured.
- [x] Health, metrics, correlation IDs, and audit events are available.

## Deployment-owner controls

- [ ] Terminate TLS 1.2 or newer at a trusted ingress.
- [ ] Use a managed OAuth issuer; do not deploy the local Keycloak fixture.
- [ ] Store credentials in a secret manager and define a rotation schedule.
- [ ] Restrict PostgreSQL and management endpoints to private networks.
- [ ] Apply ingress request-size, timeout, and per-principal rate limits.
- [ ] Configure immutable log retention without request bodies or bearer tokens.
- [ ] Define database recovery point and recovery time objectives.
- [ ] Automate encrypted backups and complete a documented restore drill.
- [ ] Monitor authentication failures, concurrency conflicts, health, and error rates.
- [ ] Pin release images by digest and verify their provenance.
- [ ] Complete an independent penetration test before handling real funds.
- [ ] Establish incident response, vulnerability disclosure, and rollback owners.

Unchecked deployment-owner controls are not repository defects; they require
environment-specific infrastructure and accountable operators. They are hard
release blockers for any system that handles real customer funds.

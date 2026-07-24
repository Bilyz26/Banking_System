# Banking System Threat Model

## Scope

This model covers the version 1.0 REST API, PostgreSQL persistence, OAuth 2.0
resource-server boundary, container image, and operational endpoints. It does
not claim that the local Keycloak development service is a production identity
platform.

## Assets

- customer identity and contact data;
- account ownership, status, balances, and versions;
- immutable financial ledger entries;
- OAuth access tokens and signing-key trust;
- database credentials and deployment secrets;
- audit, correlation, and operational telemetry.

## Trust boundaries

```text
client -> TLS ingress -> banking API -> PostgreSQL
             |               |
             |               +-> metrics and structured logs
             +-> OAuth issuer and signing keys
```

The ingress authenticates the network connection and applies traffic controls.
The application validates token signature, issuer, lifetime, and scopes. JDBC
repositories use parameterized statements. PostgreSQL is reachable only from
the private deployment network in production.

## Threats and controls

| Threat | Primary controls | Residual action |
|---|---|---|
| Unauthorized account access | JWT signature, issuer, expiry, and scope enforcement; default deny | Production issuer and key rotation must be monitored. |
| Replay or duplicated money operation | Required idempotency keys and unique ledger transaction constraints | Clients must retain keys until an operation outcome is known. |
| Concurrent balance corruption | Optimistic locking and atomic database transactions | Alert on repeated concurrency failures. |
| Ledger tampering | Append-only application boundary and paired transfer records | Restrict database write access and protect backups. |
| SQL injection | Spring JDBC parameter binding and validated identifiers | Security scans and review remain mandatory. |
| Credential disclosure | Environment/secret-store injection, ignored `.env`, no token logging | Rotate any credential suspected of exposure. |
| Token or personal-data leakage | Stable security errors and no request-body or authorization-header logging | Log access and retention require operator controls. |
| Brute force or traffic exhaustion | Rate limiting and request-size limits at the trusted ingress | The application does not implement distributed rate limiting. |
| Dependency or build compromise | Dependency review, Dependabot, pinned security scanner, repository and image scans | Review action updates and pin third-party actions by commit. |
| Container breakout | Non-root runtime, read-only filesystem, no-new-privileges, minimal JRE image | Production runtime should add platform policy and network isolation. |
| Data loss | PostgreSQL backups, restore drills, and migration-aware recovery | Deployment owner must define RPO and RTO. |

## Explicit non-goals for version 1.0

- card processing, payment-network integration, and regulatory certification;
- multi-region consensus or active-active balance writes;
- production identity-provider provisioning;
- fraud detection and transaction risk scoring.

Any deployment representing real funds requires a separate regulatory,
operational-risk, and penetration-testing program.

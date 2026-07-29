# Deployment Guide

## Local secured environment

Copy `.env.example` to `.env`, then run:

```shell
docker compose up --build --detach --wait
```

The local environment contains PostgreSQL, Keycloak, the API, and the banking
frontend. Open the frontend at `http://localhost:3000`; the API remains
available at `http://localhost:8080`. This environment is for development and
verification only. Validate it with:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\verify-local-deployment.ps1
```

## Production contract

The container requires these active profiles:

```text
postgresql,secure,production
```

Required external configuration:

- `BANKING_DB_URL`
- `BANKING_DB_USERNAME`
- `BANKING_DB_PASSWORD`
- `BANKING_JWT_ISSUER_URI`
- `BANKING_JWT_JWK_SET_URI`

Optional pool and shutdown settings are documented in
`application-postgresql.yml` and `application-production.yml`.

Production must use a managed PostgreSQL service or equivalent protected
cluster, a managed OAuth issuer, TLS ingress, private networks, a secret
manager, centralized logs, metrics/alerts, and automated encrypted backups.
The repository's Compose Keycloak service and development credentials must not
be promoted to production.

## Health and shutdown

- liveness: `/actuator/health/liveness`
- readiness: `/actuator/health/readiness`

Remove an instance from traffic when readiness fails. Send `SIGTERM` and allow
at least the configured graceful-shutdown interval before forceful
termination.

## Database changes and recovery

Flyway applies forward migrations during startup. Back up the database before
deploying a migration. Test the release and migration against a restored copy
of production data with sensitive values protected.

Rollback of application code does not imply rollback of a database migration.
Every deployment plan must confirm schema compatibility. Operators must define
RPO/RTO targets, automate encrypted backups, and regularly demonstrate a
successful restore.

## Release image

Build from the signed release tag and record the resulting image digest:

```shell
docker build --tag banking-system:1.0.0 .
docker image inspect banking-system:1.0.0 --format '{{.Id}}'
```

Scan the exact image and deploy by immutable digest rather than a mutable tag.

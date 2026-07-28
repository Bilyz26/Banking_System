# Frontend 0.1.0

Initial production-oriented frontend release.

## Included

- OAuth 2.0 Authorization Code with PKCE authentication
- responsive and accessible banking application shell
- dashboard, customer, account, deposit, withdrawal, transfer, and
  transaction-history workflows
- runtime-validated API contracts and structured error handling
- idempotency protection and ambiguous-outcome receipts for money movement
- unit, component, accessibility, browser, and deployment smoke tests
- route-level code splitting and enforced compressed bundle budgets
- hardened non-root Nginx container and same-origin backend proxy

## Deployment note

The packaged Nginx configuration is for the documented local environment.
Before deploying publicly, use an exact HTTPS Keycloak origin in the Content
Security Policy and provide TLS, HSTS, rate limiting, and monitoring at the
production ingress.

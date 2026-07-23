# ADR 0009: OAuth 2.0 JWT resource server

## Status

Accepted

## Context

The REST API changes customer data and account balances but previously accepted
anonymous requests. Building password storage and token issuance into the
banking application would mix identity-provider responsibilities with banking
business logic.

## Decision

Delegate authentication and token issuance to an OAuth 2.0 authorization
server. Under the `secure` profile, configure this application as a stateless
JWT resource server and authorize requests using:

- `banking.read` for account queries;
- `banking.write` for money movement;
- `banking.admin` for customer, account creation, and lifecycle administration.

Keep security configuration at the bootstrap and HTTP boundary. Domain and
application classes remain independent of Spring Security and JWT claims.

The non-secure profile explicitly permits requests for local development only.
Production deployments must activate `secure`, normally together with
`postgresql`.

## Consequences

- the application does not store passwords or issue tokens;
- authorization rules are centralized and testable;
- API processing is stateless and horizontally scalable;
- the authorization server must publish signing keys and grant the documented
  scopes;
- account ownership authorization is not yet modeled, so these scopes are best
  suited to trusted service clients rather than retail customer access;
- CSRF protection is disabled because authentication uses bearer headers rather
  than browser cookies.

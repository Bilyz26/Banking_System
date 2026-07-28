# Frontend Security and Performance Review

## Scope

This review covers the browser application, its Nginx runtime image, the backend
proxy boundary, dependency risk, and production bundle behavior.

## Security controls

- OAuth 2.0 Authorization Code with PKCE delegates authentication to Keycloak.
- Tokens are held in session storage and are never written to local storage,
  cookies, logs, URLs, or application state persisted across browser sessions.
- The API client adds bearer tokens at the network boundary and validates API
  responses before feature code receives them.
- Nginx applies a restrictive Content Security Policy, denies framing, disables
  MIME sniffing, removes referrer data, restricts browser capabilities, and
  isolates the browsing context.
- The backend is reached through the same-origin `/backend/` proxy, keeping API
  topology out of feature code and avoiding permissive cross-origin rules.
- Request bodies are limited to 1 MiB and upstream server identification is
  hidden.
- CI blocks high and critical dependency, configuration, secret, and container
  findings.

The checked-in Content Security Policy targets the documented local Keycloak
deployment at `http://localhost:9000`. A non-local deployment must replace that
origin with its exact HTTPS identity-provider origin. Wildcards must not be
introduced.

## Performance controls

- Every feature page is loaded through a dynamic import, so the initial route
  does not download unrelated banking workflows.
- Vite emits content-hashed assets; Nginx caches those immutable assets for one
  year while preventing caching of `index.html`.
- `npm run build` enforces two gzip budgets: no JavaScript chunk may exceed
  120 KiB and the complete generated site may not exceed 180 KiB.
- CI retains the production bundle, coverage report, and browser-test report for
  diagnosis.

## Residual risks and deployment requirements

- TLS termination, HSTS, certificate rotation, WAF policy, and rate limiting
  belong at the production ingress and are intentionally not simulated by the
  local Nginx container.
- Content Security Policy reporting should be connected to the operator's
  reporting endpoint before public launch.
- Source maps are not published by the production build. If an error-monitoring
  service needs them, upload them privately during CI and do not serve them.
- Dependency and container scans are point-in-time controls; automated update
  PRs and regular image rebuilds remain operational requirements.

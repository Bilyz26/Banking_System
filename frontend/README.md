# Banking System Frontend

The frontend is a React and TypeScript single-page application for the Banking
System API. It uses feature-owned packages, runtime-validated API contracts,
OAuth 2.0 Authorization Code with PKCE, and an accessible shared design system.

## Requirements

- Node.js 20.17 or newer
- npm 10 or newer
- Chromium for browser tests

## Local development

Install the exact locked dependency graph:

```shell
npm ci
```

Copy `.env.example` to `.env.local`, then start the development server:

```shell
npm run dev
```

The application is available at `http://localhost:5173`. Its development proxy
forwards `/backend` to the API at `http://localhost:8080`.

Environment variables:

| Variable               | Purpose                     | Local value                            |
| ---------------------- | --------------------------- | -------------------------------------- |
| `VITE_API_BASE_URL`    | Same-origin API prefix      | `/backend`                             |
| `VITE_OIDC_ISSUER_URL` | Exact Keycloak realm issuer | `http://localhost:9000/realms/banking` |
| `VITE_OIDC_CLIENT_ID`  | Public PKCE client          | `banking-web`                          |

Only public browser configuration may use the `VITE_` prefix. Secrets must
never be embedded in a frontend build.

## Quality commands

```shell
npm run verify
npm run test:e2e
npm audit --audit-level=high
```

`verify` checks formatting, lint rules, TypeScript, unit/component/accessibility
tests with coverage thresholds, the production build, and gzip bundle budgets.
The Playwright suite covers the authentication boundary at desktop and mobile
viewports.

## Architecture

- `src/app` owns composition, providers, routing, and the application shell.
- `src/features` owns user-facing banking capabilities.
- `src/shared/api` owns the authenticated transport and runtime contracts.
- `src/shared/design-system` owns reusable accessible visual primitives.
- `src/shared/config` validates browser configuration at startup.
- `e2e` owns browser-level journeys.

Feature code may depend on shared code; shared code must not depend on features.
Business rules remain in the Java backend and are never duplicated as an
authoritative frontend decision.

See [architecture](../docs/frontend/architecture.md),
[testing](../docs/frontend/testing.md), and the
[security/performance review](../docs/frontend/security-performance-review.md).

## Container operation

From the repository root:

```shell
docker compose up --build --detach --wait
```

The Nginx container serves the application at `http://localhost:3000` and
proxies `/backend/` to the API container. The checked-in CSP permits only the
local Keycloak issuer; production deployments must set an exact HTTPS identity
provider origin.

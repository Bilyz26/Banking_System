# Frontend Testing Strategy

The test portfolio follows risk rather than implementation detail.

## Layers

| Layer            | Tool                   | Primary responsibility                               |
| ---------------- | ---------------------- | ---------------------------------------------------- |
| Pure unit        | Vitest                 | permissions, validation, cursor/path rules           |
| Component        | Testing Library        | user-visible behavior and keyboard interaction       |
| Accessibility    | axe-core               | automatically detectable WCAG violations             |
| API boundary     | Vitest                 | bearer tokens, runtime contracts, structured errors  |
| Browser smoke    | Playwright             | authentication boundary and responsive usability     |
| Deployment smoke | PowerShell and Compose | real identity, API, persistence, and frontend wiring |

Tests should query elements by role, label, and visible name. CSS selectors and
implementation-specific component state are not stable user contracts.

## Local verification

```shell
npm run verify
npm run test:e2e
```

`npm run verify` fails if coverage drops below the configured thresholds or the
generated bundle exceeds its gzip budgets. Playwright starts an isolated Vite
server and tests Chromium with desktop and mobile profiles.

The browser smoke suite intentionally stops at the authentication boundary.
The complete authenticated data journey is exercised against the real Compose
stack by `scripts/verify-local-deployment.ps1`, avoiding insecure test-only
authentication shortcuts in the browser application.

## Pull-request policy

A frontend change is ready for review only when:

- locked installation and dependency audit succeed;
- formatting, lint, types, coverage, and build succeed;
- browser smoke tests succeed;
- the frontend container builds;
- no new high or critical security finding is introduced.

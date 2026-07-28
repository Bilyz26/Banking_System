# Frontend Product and UX Requirements

## 1. Purpose

The Banking System frontend is a responsive banking-operations portal for
authenticated staff. It provides a trustworthy interface to the existing
customer, account, money-operation, and transaction-history APIs.

The first frontend release prioritizes correctness, clarity, security, and
accessibility over decorative effects. It adopts the approved visual direction:
an obsidian interface, restrained glass surfaces, crimson primary accents,
blush secondary accents, and clear success states.

## 2. Product principles

1. **Server truth over simulation.** The interface must never fabricate
   balances, transactions, successful operations, or authorization.
2. **Financial actions are deliberate.** Money movement requires validation,
   review, explicit confirmation, duplicate-submission protection, and a
   server-confirmed receipt.
3. **Permissions are visible.** Navigation and actions reflect granted OAuth
   scopes, while the API remains the final authorization authority.
4. **Failures are recoverable.** Error messages explain what happened, whether
   an operation may have completed, and the safest next action.
5. **Accessibility is foundational.** All released workflows target WCAG 2.2
   Level AA and remain usable with keyboard, screen reader, zoom, high contrast,
   and reduced motion.
6. **Responsive means task-complete.** Mobile layouts preserve the same
   essential banking capabilities as desktop layouts.

## 3. Users and authorization

The version-one frontend supports authenticated banking staff. It does not
claim to be an end-customer self-service portal because the current API exposes
administrative customer and account operations.

| Capability | Required OAuth scope | Frontend behavior |
| --- | --- | --- |
| View customers, accounts, and transactions | `banking.read` | Show read-only detail and history routes. |
| Deposit, withdraw, and transfer | `banking.write` | Show money-operation actions with confirmation. |
| Create/update customers and manage accounts | `banking.admin` | Show administrative forms and lifecycle actions. |
| View operational monitoring | `banking.monitor` | Reserved for a future operations screen. |

The interface may hide unavailable actions to reduce confusion, but it must
also handle `401` and `403` responses because client-side checks are not a
security boundary.

## 4. Supported workflows

### 4.1 Authentication

- Sign in through the configured OpenID Connect provider.
- Return users to their intended protected route after successful sign-in.
- Refresh or renew authentication without silently losing unsaved form data.
- Explain expired sessions and provide a safe sign-in path.
- Sign out locally and at the identity provider where supported.

### 4.2 Dashboard

- Display an account only after the operator selects or retrieves it.
- Show the current balance, currency, status, and recent transactions from the
  API.
- Provide permission-aware shortcuts to deposit, withdraw, and transfer.
- Allow balances to be masked without changing stored data.
- Represent loading, empty, partial failure, unavailable, and complete states.

The API does not currently provide portfolio totals, time-series balance data,
spending categories, cards, currency exchange, or payment requests. The first
release must not invent these capabilities.

### 4.3 Customer management

- Create a customer.
- Retrieve a customer by identifier.
- Display customer details.
- Update editable customer profile fields.
- Present field validation and business conflicts without discarding input.

The API does not provide customer listing or search. The initial interface uses
direct identifier lookup and may keep a non-persistent recent-items list in the
current session. Global search requires a future backend endpoint.

### 4.4 Account management

- Open an account for an existing customer.
- Retrieve an account by identifier.
- Display balance, currency, version, and lifecycle status.
- Freeze, unfreeze, or close an account when authorized and valid.
- Require confirmation for lifecycle changes.

The API does not provide account listing. Account navigation therefore starts
with a known account identifier or the result of opening an account.

### 4.5 Money operations

- Deposit into an account.
- Withdraw from an account.
- Transfer between accounts.
- Generate a UUID idempotency key for each intended operation.
- Retain the same key when safely retrying an ambiguous network failure.
- Present a review step before submission.
- Disable repeated submission while a request is in progress.
- Display the immutable transaction reference returned by the server.
- Never report success from a failed, missing, or unparseable response.

### 4.6 Transaction history

- Display newest-first account transactions.
- Distinguish credit and debit using text, sign, icon, and color.
- Show type, amount, currency, occurrence time, and references.
- Follow opaque cursor pagination without parsing or constructing cursors.
- Provide readable table presentation on wide screens and cards on narrow
  screens.

## 5. Information architecture

```text
Sign in
└── Banking workspace
    ├── Dashboard
    ├── Customers
    │   ├── Find customer
    │   ├── Create customer
    │   └── Customer details / edit
    ├── Accounts
    │   ├── Find account
    │   ├── Open account
    │   └── Account details / lifecycle
    ├── Money operations
    │   ├── Deposit
    │   ├── Withdrawal
    │   └── Transfer
    ├── Transactions
    │   └── Account transaction history
    └── User menu
        ├── Session and permissions
        └── Sign out
```

Routes that require unavailable scopes show a clear access-denied page rather
than a blank screen or redirect loop.

## 6. Experience states

Every data-driven screen must define:

- initial and loading states;
- successful state;
- no-data state;
- field-validation state;
- authentication-required state;
- insufficient-permission state;
- business-rule conflict state;
- service-unavailable state;
- ambiguous financial-operation state; and
- unexpected error state with a correlation identifier when supplied.

Financial requests that time out after submission must not be labeled failed.
The interface instructs the operator to retry with the retained idempotency key
or verify transaction history.

## 7. Visual direction

### 7.1 Semantic palette

| Token intent | Initial value | Usage |
| --- | --- | --- |
| Canvas | `#0B0E14` | Primary application background |
| Elevated surface | `rgba(22, 27, 38, 0.92)` | Cards and overlays |
| Surface border | `rgba(255, 255, 255, 0.10)` | Structural boundaries |
| Primary | `#FF2E4C` | Primary actions and active navigation |
| Secondary | `#FFB3C6` | Restrained highlights |
| Success | `#10B981` | Confirmed successful states |
| Information | `#00B8CC` | Informational states and focus accents |

Color names in implementation must describe semantic purpose rather than a
third-party brand. Status is never communicated by color alone.

### 7.2 Typography and shape

- Plus Jakarta Sans is the preferred interface family, with a system fallback.
- Financial values use tabular numerals.
- Body text remains at least 16 CSS pixels by default.
- Standard cards use a 16-pixel radius; feature cards may use 24 pixels.
- Interactive targets are at least 44 by 44 CSS pixels.
- Glass effects remain restrained enough to preserve text contrast.

### 7.3 Motion

- Motion communicates state or spatial relationships and is not decorative
  noise.
- Hover movement is subtle and does not move essential financial information.
- All non-essential animation is removed when reduced motion is requested.

## 8. Responsive support

The supported viewport baseline is 320 CSS pixels through wide desktop.

- **Mobile:** single-column task flows and bottom or compact navigation.
- **Tablet:** adaptive navigation and one- or two-column content.
- **Desktop:** persistent sidebar and multi-column overview where useful.

No critical action depends on hover. Tables transform into labeled cards or
allow controlled horizontal scrolling without losing row context.

## 9. Accessibility acceptance criteria

- All functions are available by keyboard.
- Focus order follows visual and task order.
- Focus remains visible and is restored after dialogs close.
- Dialogs use correct names, descriptions, focus trapping, and Escape behavior.
- Forms have persistent labels, instructions, and programmatically associated
  errors.
- Status changes use appropriate live regions without excessive announcements.
- Text and controls meet WCAG 2.2 AA contrast requirements.
- The interface works at 200 percent browser zoom and with text resizing.
- Icons have accessible names when meaningful and are hidden when decorative.
- Charts, if later supported by real data, include equivalent textual values.

## 10. Security and privacy requirements

- Use Authorization Code Flow with PKCE through Keycloak.
- Do not persist access or refresh tokens in `localStorage`.
- Do not place tokens, personal data, or account details in URLs or logs.
- Mask sensitive financial values on request.
- Escape untrusted content and avoid raw HTML rendering.
- Apply a restrictive Content Security Policy in production.
- Use HTTPS outside local development.
- Clear transient sensitive state during sign-out.
- Treat backend authorization and validation as authoritative.

## 11. Supported environments

- Latest two stable versions of Chrome, Edge, Firefox, and Safari.
- Responsive web operation on current iOS Safari and Android Chrome.
- Local development against the existing Spring Boot and Keycloak Compose
  environment.
- Production builds served separately from the API unless a later architecture
  decision records another deployment model.

## 12. Out of scope for the first frontend release

- Physical or virtual payment-card management and CVV display
- Currency exchange
- Payment requests
- Merchant enrichment and external merchant logos
- Portfolio or investment analytics
- Spending-category analytics
- Push notifications
- Customer self-registration
- Customer or account search/listing without backend support
- A light theme

These features require explicit product requirements, security review, and
supporting backend APIs before UI implementation.

## 13. Definition of done

The frontend release is complete when:

1. Supported workflows use the version-controlled OpenAPI contract.
2. Authentication and scope behavior work against the local Keycloak realm.
3. Critical financial flows pass automated end-to-end tests.
4. Unit, component, accessibility, type, lint, and production-build checks pass.
5. Responsive acceptance is verified at mobile, tablet, and desktop sizes.
6. No UI path simulates financial success or unsupported backend data.
7. Docker, CI, security scanning, deployment, and contributor documentation are
   complete.

## 14. Decisions carried into architecture

- Build a staff banking-operations portal for version one.
- Use EUR as the default display currency while always honoring the account's
  server-provided ISO 4217 currency.
- Use the existing OAuth scopes as frontend capability boundaries.
- Keep unsupported concept features out of navigation.
- Replace the prototype rather than evolving its simulated-data behavior.
- Target WCAG 2.2 Level AA and 320-pixel responsive support.

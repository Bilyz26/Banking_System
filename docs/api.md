# Banking API

All endpoints use the `/api/v1` prefix and exchange JSON.

The machine-readable OpenAPI 3.1 contract is available at
[`docs/openapi/banking-api.json`](openapi/banking-api.json). It is maintained
as a reviewed source artifact and verified against the registered Spring routes
during every build. REST record fields are also compared with their mapped
OpenAPI schemas. CI publishes the verified contract as a seven-day build
artifact named `banking-system-openapi-<commit-sha>`. Java field types, UUID
and timestamp formats, collection items, and enum values are checked against
the same schemas. Required fields, string limits, patterns, decimal bounds, and
precision are checked against Jakarta validation annotations.

## Authentication and authorization

When the `secure` profile is active, every endpoint requires an
`Authorization: Bearer <token>` header. The JWT is validated against the issuer
configured by `BANKING_JWT_ISSUER_URI`.

Required OAuth 2.0 scopes:

| Scope | Operations |
| --- | --- |
| `banking.read` | Retrieve customers, accounts, and account transaction history |
| `banking.write` | Deposit, withdraw, and transfer money |
| `banking.admin` | Create and update customers; create, freeze, unfreeze, and close accounts |
| `banking.monitor` | Read application info, diagnostic metrics, and Prometheus metrics |

Missing or invalid authentication returns `401` with
`AUTHENTICATION_REQUIRED`. A valid token without the required scope returns
`403` with `INSUFFICIENT_SCOPE`.

## Operational endpoints

The following endpoints are exposed outside the `/api/v1` prefix:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/metrics/{metricName}`
- `GET /actuator/prometheus`

Health responses never expose component details. Under the `secure` profile,
health probes are anonymous so an orchestrator can determine availability.
Other operational endpoints require `banking.monitor`.

Every response includes `X-Correlation-ID`. A caller-supplied value is accepted
when it contains 1–64 ASCII letters, digits, dots, underscores, or hyphens.
Otherwise, the server generates a UUID.

## Customers

- `POST /customers` creates a customer.
- `GET /customers/{customerId}` retrieves a customer.
- `PUT /customers/{customerId}` replaces a customer's editable profile.

## Accounts

- `POST /accounts` opens an account for an existing customer.
- `GET /accounts/{accountId}` retrieves an account.
- `GET /accounts/{accountId}/transactions` retrieves transaction history in
  newest-first order.
- `POST /accounts/{accountId}/freeze` freezes an active account.
- `POST /accounts/{accountId}/unfreeze` reactivates a frozen account.
- `POST /accounts/{accountId}/close` closes a zero-balance account.
- `POST /accounts/{accountId}/deposits` deposits money.
- `POST /accounts/{accountId}/withdrawals` withdraws money.

Transaction history accepts an optional `limit` from 1 to 100 (default 20) and
an optional opaque `cursor`. When `nextCursor` is present in a response, pass it
unchanged as the next request's `cursor`. Clients must not parse or construct
cursors. The stable sort key combines occurrence time and ledger-entry ID so
entries with identical timestamps are not skipped or repeated.

## Transfers

- `POST /transfers` transfers money between two accounts.

## Idempotency

Deposits, withdrawals, and transfers require an `Idempotency-Key` request
header containing a UUID. The UUID becomes the immutable ledger transaction
identity.

Retrying an identical request with the same key returns the original successful
response without changing account balances again. Reusing a key for a different
operation, account, amount, currency, or description returns HTTP `409` with
the error code `IDEMPOTENCY_KEY_REUSED`.

Clients must generate a new key for each intended money operation and retain it
when retrying after a timeout or transient connection failure.

Money requests represent `amount` as a JSON number and `currencyCode` as an
uppercase ISO 4217 code. Each successful money operation returns its immutable
ledger identity and transaction identity.

Validation and business failures use the shared `ApiError` response contract.

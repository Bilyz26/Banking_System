# Banking API

All endpoints use the `/api/v1` prefix and exchange JSON.

## Customers

- `POST /customers` creates a customer.

## Accounts

- `POST /accounts` opens an account for an existing customer.
- `GET /accounts/{accountId}` retrieves an account.
- `POST /accounts/{accountId}/freeze` freezes an active account.
- `POST /accounts/{accountId}/unfreeze` reactivates a frozen account.
- `POST /accounts/{accountId}/close` closes a zero-balance account.
- `POST /accounts/{accountId}/deposits` deposits money.
- `POST /accounts/{accountId}/withdrawals` withdraws money.

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

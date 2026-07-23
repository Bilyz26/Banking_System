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

Money requests represent `amount` as a JSON number and `currencyCode` as an
uppercase ISO 4217 code. Each successful money operation returns its immutable
ledger identity and transaction identity.

Validation and business failures use the shared `ApiError` response contract.

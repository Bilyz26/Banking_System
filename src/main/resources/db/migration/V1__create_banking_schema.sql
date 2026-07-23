CREATE TABLE customers (
    customer_id UUID PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email_address VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE accounts (
    account_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES customers (customer_id),
    balance NUMERIC(20, 3) NOT NULL CHECK (balance >= 0),
    currency_code VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0)
);

CREATE TABLE ledger_entries (
    ledger_entry_id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL REFERENCES accounts (account_id),
    entry_type VARCHAR(30) NOT NULL,
    amount NUMERIC(20, 3) NOT NULL CHECK (amount > 0),
    balance_after NUMERIC(20, 3) NOT NULL CHECK (balance_after >= 0),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(500) NOT NULL
);

CREATE INDEX idx_ledger_entries_account_occurred
    ON ledger_entries (account_id, occurred_at, ledger_entry_id);

CREATE INDEX idx_ledger_entries_transaction
    ON ledger_entries (transaction_id);

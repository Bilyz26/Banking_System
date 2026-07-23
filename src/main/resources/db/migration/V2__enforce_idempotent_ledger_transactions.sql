ALTER TABLE ledger_entries
    ADD CONSTRAINT uq_ledger_transaction_entry_type
        UNIQUE (transaction_id, entry_type);

CREATE INDEX idx_ledger_entries_transaction_id
    ON ledger_entries (transaction_id);

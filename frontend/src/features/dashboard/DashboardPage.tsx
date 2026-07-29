import { useQuery } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { z } from "zod";

import { Button } from "../../shared/design-system/components/Button";
import { StatusBadge } from "../../shared/design-system/components/StatusBadge";
import { Surface } from "../../shared/design-system/components/Surface";
import {
  accountSchema,
  transactionPageSchema,
} from "../../shared/api/contracts";
import { useBankingApi } from "../../shared/api/useBankingApi";
import styles from "./DashboardPage.module.css";

const accountIdSchema = z.uuid("Enter a valid account UUID.");

export function DashboardPage() {
  const api = useBankingApi();
  const [input, setInput] = useState("");
  const [accountId, setAccountId] = useState<string>();
  const [validationError, setValidationError] = useState<string>();

  const accountQuery = useQuery({
    queryKey: ["account", accountId],
    queryFn: () => api.get(`/api/v1/accounts/${accountId}`, accountSchema),
    enabled: Boolean(accountId),
  });
  const transactionQuery = useQuery({
    queryKey: ["transactions", accountId],
    queryFn: () =>
      api.get(
        `/api/v1/accounts/${accountId}/transactions?limit=5`,
        transactionPageSchema,
      ),
    enabled: Boolean(accountId && accountQuery.data),
  });

  function retrieveAccount(event: FormEvent) {
    event.preventDefault();
    const parsed = accountIdSchema.safeParse(input.trim());
    if (!parsed.success) {
      setValidationError(parsed.error.issues[0]?.message);
      return;
    }
    setValidationError(undefined);
    setAccountId(parsed.data);
  }

  const account = accountQuery.data;

  return (
    <main>
      <div className={styles.heading}>
        <div>
          <StatusBadge tone="success">Secure workspace</StatusBadge>
          <h1>Dashboard</h1>
          <p className={styles.muted}>
            Retrieve an account to display verified data.
          </p>
        </div>
      </div>

      <Surface>
        <form className={styles.lookup} onSubmit={retrieveAccount}>
          <label>
            Account identifier
            <input
              aria-describedby={
                validationError ? "account-id-error" : undefined
              }
              aria-invalid={Boolean(validationError)}
              onChange={(event) => setInput(event.target.value)}
              placeholder="00000000-0000-0000-0000-000000000000"
              value={input}
            />
          </label>
          <Button busy={accountQuery.isFetching} type="submit">
            Retrieve account
          </Button>
        </form>
        {validationError && (
          <p id="account-id-error" role="alert">
            {validationError}
          </p>
        )}
        {accountQuery.error && <p role="alert">{accountQuery.error.message}</p>}
      </Surface>

      {account && (
        <div className={styles.grid}>
          <Surface aria-labelledby="balance-title">
            <StatusBadge
              tone={account.status === "ACTIVE" ? "success" : "warning"}
            >
              {account.status}
            </StatusBadge>
            <h2 id="balance-title">Available balance</h2>
            <p className={styles.balance}>
              {new Intl.NumberFormat(undefined, {
                style: "currency",
                currency: account.currencyCode,
              }).format(account.balance)}
            </p>
            <p className={styles.muted}>Account {account.accountId}</p>
            <p className={styles.muted}>Owner {account.ownerId}</p>
          </Surface>

          <Surface aria-labelledby="recent-title">
            <h2 id="recent-title">Recent transactions</h2>
            {transactionQuery.isLoading && <p>Loading transactions…</p>}
            {transactionQuery.error && (
              <p role="alert">{transactionQuery.error.message}</p>
            )}
            {transactionQuery.data?.transactions.length === 0 && (
              <p>No transactions have been recorded.</p>
            )}
            <ul className={styles.transactions}>
              {transactionQuery.data?.transactions.map((transaction) => (
                <li
                  className={styles.transaction}
                  key={transaction.ledgerEntryId}
                >
                  <span>
                    <strong>{transaction.type.replaceAll("_", " ")}</strong>
                    <br />
                    <small>
                      {new Date(transaction.occurredAt).toLocaleString()}
                    </small>
                  </span>
                  <strong>
                    {new Intl.NumberFormat(undefined, {
                      style: "currency",
                      currency: transaction.currencyCode,
                    }).format(transaction.amount)}
                  </strong>
                </li>
              ))}
            </ul>
          </Surface>
        </div>
      )}
    </main>
  );
}

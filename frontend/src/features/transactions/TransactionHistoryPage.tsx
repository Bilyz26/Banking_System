import { useInfiniteQuery } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { z } from "zod";

import { transactionPageSchema } from "../../shared/api/contracts";
import { useBankingApi } from "../../shared/api/useBankingApi";
import { Button } from "../../shared/design-system/components/Button";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "./TransactionHistoryPage.module.css";
import { buildTransactionHistoryPath } from "./transactionHistoryPath";

const accountIdSchema = z.uuid("Enter a valid account UUID.");

export function TransactionHistoryPage() {
  const api = useBankingApi();
  const [lookupInput, setLookupInput] = useState("");
  const [accountId, setAccountId] = useState<string>();
  const [message, setMessage] = useState<string>();

  const history = useInfiniteQuery({
    queryKey: ["transactions", accountId, "history"],
    queryFn: ({ pageParam }) =>
      api.get(
        buildTransactionHistoryPath(accountId ?? "", pageParam),
        transactionPageSchema,
      ),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (page) => page.nextCursor ?? undefined,
    enabled: Boolean(accountId),
  });

  function lookup(event: FormEvent) {
    event.preventDefault();
    const result = accountIdSchema.safeParse(lookupInput.trim());
    setMessage(result.success ? undefined : result.error.issues[0]?.message);
    if (result.success) setAccountId(result.data);
  }

  const transactions =
    history.data?.pages.flatMap((page) => page.transactions) ?? [];

  return (
    <main>
      <h1>Transaction history</h1>
      <p>Review immutable ledger entries in newest-first order.</p>
      <Surface>
        <form className={styles.lookup} onSubmit={lookup}>
          <label>
            Account identifier
            <input
              onChange={(event) => setLookupInput(event.target.value)}
              value={lookupInput}
            />
          </label>
          <Button
            busy={history.isFetching && !history.isFetchingNextPage}
            type="submit"
          >
            Retrieve history
          </Button>
        </form>
        {message && <p role="status">{message}</p>}
        {history.error && <p role="alert">{history.error.message}</p>}
      </Surface>

      {accountId && !history.isLoading && !history.error && (
        <Surface
          className={styles.tableWrapper}
          aria-labelledby="history-results"
        >
          <h2 id="history-results">Ledger entries</h2>
          {transactions.length === 0 ? (
            <p>No transactions have been recorded for this account.</p>
          ) : (
            <table className={styles.table}>
              <caption className="visually-hidden">
                Transactions for account {accountId}
              </caption>
              <thead>
                <tr>
                  <th scope="col">Date</th>
                  <th scope="col">Type</th>
                  <th scope="col">Description</th>
                  <th scope="col">Amount</th>
                  <th scope="col">Balance after</th>
                  <th scope="col">Reference</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((transaction) => {
                  const credit =
                    transaction.type === "DEPOSIT" ||
                    transaction.type === "TRANSFER_CREDIT";
                  const formattedAmount = new Intl.NumberFormat(undefined, {
                    style: "currency",
                    currency: transaction.currencyCode,
                  }).format(transaction.amount);
                  return (
                    <tr key={transaction.ledgerEntryId}>
                      <td>
                        {new Date(transaction.occurredAt).toLocaleString()}
                      </td>
                      <td>{transaction.type.replaceAll("_", " ")}</td>
                      <td>{transaction.description || "No description"}</td>
                      <td
                        className={`${styles.amount} ${credit ? styles.credit : styles.debit}`}
                      >
                        {credit ? "+" : "−"}
                        {formattedAmount}
                      </td>
                      <td>
                        {new Intl.NumberFormat(undefined, {
                          style: "currency",
                          currency: transaction.currencyCode,
                        }).format(transaction.balanceAfter)}
                      </td>
                      <td
                        className={styles.reference}
                        title={transaction.transactionId}
                      >
                        {transaction.transactionId}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
          {history.hasNextPage && (
            <div className={styles.footer}>
              <Button
                busy={history.isFetchingNextPage}
                onClick={() => void history.fetchNextPage()}
                variant="secondary"
              >
                Load older transactions
              </Button>
            </div>
          )}
        </Surface>
      )}
    </main>
  );
}

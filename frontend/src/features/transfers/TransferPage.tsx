import { useMutation, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { useAuth } from "react-oidc-context";

import { hasScope, parseScopes } from "../authentication/permissions";
import {
  transferRequestSchema,
  transferResponseSchema,
  type TransferRequest,
} from "../../shared/api/contracts";
import { BankingApiError } from "../../shared/api/BankingApiClient";
import { useBankingApi } from "../../shared/api/useBankingApi";
import { Button } from "../../shared/design-system/components/Button";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "../../shared/design-system/workspace.module.css";

const initialDraft = {
  sourceAccountId: "",
  destinationAccountId: "",
  amount: "",
  currencyCode: "EUR",
  description: "",
};

export function TransferPage() {
  const api = useBankingApi();
  const auth = useAuth();
  const queryClient = useQueryClient();
  const canWrite = hasScope(parseScopes(auth.user?.scope), "banking.write");
  const [draft, setDraft] = useState(initialDraft);
  const [reviewed, setReviewed] = useState<TransferRequest>();
  const [requestKey, setRequestKey] = useState<string>();
  const [message, setMessage] = useState<string>();

  const transfer = useMutation({
    mutationFn: async () => {
      if (!reviewed || !requestKey) throw new Error("Review is required.");
      return api.post("/api/v1/transfers", reviewed, transferResponseSchema, {
        "Idempotency-Key": requestKey,
      });
    },
    onSuccess: (receipt) => {
      for (const id of [
        receipt.sourceAccountId,
        receipt.destinationAccountId,
      ]) {
        void queryClient.invalidateQueries({ queryKey: ["account", id] });
        void queryClient.invalidateQueries({ queryKey: ["transactions", id] });
      }
    },
  });

  function update(field: keyof typeof draft, value: string) {
    setDraft((current) => ({ ...current, [field]: value }));
  }

  function review(event: FormEvent) {
    event.preventDefault();
    const result = transferRequestSchema.safeParse({
      sourceAccountId: draft.sourceAccountId.trim(),
      destinationAccountId: draft.destinationAccountId.trim(),
      amount: Number(draft.amount),
      currencyCode: draft.currencyCode.toUpperCase(),
      description: draft.description.trim() || null,
    });
    if (!result.success) {
      setMessage(result.error.issues[0]?.message);
      return;
    }
    setReviewed(result.data);
    setRequestKey(crypto.randomUUID());
    setMessage(undefined);
    transfer.reset();
  }

  function reset() {
    setReviewed(undefined);
    setRequestKey(undefined);
    transfer.reset();
  }

  const receipt = transfer.data;
  const ambiguous =
    transfer.error && !(transfer.error instanceof BankingApiError);

  return (
    <main>
      <h1>Transfer money</h1>
      <p>Move funds between accounts with an immutable transaction identity.</p>
      {!canWrite ? (
        <Surface>
          <p>You do not have permission to transfer funds.</p>
        </Surface>
      ) : (
        <div className={styles.layout}>
          <Surface aria-labelledby="transfer-details">
            <h2 id="transfer-details">Transfer details</h2>
            <form className={styles.form} onSubmit={review}>
              <label>
                Source account
                <input
                  onChange={(event) =>
                    update("sourceAccountId", event.target.value)
                  }
                  value={draft.sourceAccountId}
                />
              </label>
              <label>
                Destination account
                <input
                  onChange={(event) =>
                    update("destinationAccountId", event.target.value)
                  }
                  value={draft.destinationAccountId}
                />
              </label>
              <label>
                Amount
                <input
                  min="0.01"
                  onChange={(event) => update("amount", event.target.value)}
                  step="0.01"
                  type="number"
                  value={draft.amount}
                />
              </label>
              <label>
                Currency
                <input
                  maxLength={3}
                  onChange={(event) =>
                    update("currencyCode", event.target.value.toUpperCase())
                  }
                  value={draft.currencyCode}
                />
              </label>
              <label>
                Description
                <input
                  maxLength={500}
                  onChange={(event) =>
                    update("description", event.target.value)
                  }
                  value={draft.description}
                />
              </label>
              <Button type="submit">Review transfer</Button>
            </form>
          </Surface>

          <Surface aria-labelledby="transfer-review">
            <h2 id="transfer-review">Review and confirmation</h2>
            {!reviewed && <p>Complete the form to review the transfer.</p>}
            {reviewed && !receipt && (
              <div className={styles.details}>
                <strong>
                  {reviewed.amount.toFixed(2)} {reviewed.currencyCode}
                </strong>
                <span className={styles.identifier}>
                  From {reviewed.sourceAccountId}
                </span>
                <span className={styles.identifier}>
                  To {reviewed.destinationAccountId}
                </span>
                <span className={styles.identifier}>Request {requestKey}</span>
                <div className={styles.actions}>
                  <Button
                    busy={transfer.isPending}
                    onClick={() => transfer.mutate()}
                  >
                    Confirm transfer
                  </Button>
                  <Button onClick={reset} variant="secondary">
                    Change details
                  </Button>
                </div>
              </div>
            )}
            {receipt && (
              <div className={styles.details} role="status">
                <h3>Transfer confirmed</h3>
                <span className={styles.identifier}>
                  Transaction {receipt.transactionId}
                </span>
                <span>
                  Source balance: {receipt.sourceBalance.toFixed(2)}{" "}
                  {receipt.currencyCode}
                </span>
                <span>
                  Destination balance: {receipt.destinationBalance.toFixed(2)}{" "}
                  {receipt.currencyCode}
                </span>
                <span className={styles.identifier}>
                  Debit {receipt.debitEntryId}
                </span>
                <span className={styles.identifier}>
                  Credit {receipt.creditEntryId}
                </span>
                <Button onClick={reset} variant="secondary">
                  Start another transfer
                </Button>
              </div>
            )}
            {transfer.error && (
              <p role="alert">
                {ambiguous
                  ? "The outcome is unknown. Retry with the same request key or verify both account histories."
                  : transfer.error.message}
              </p>
            )}
            {message && <p role="status">{message}</p>}
          </Surface>
        </div>
      )}
    </main>
  );
}

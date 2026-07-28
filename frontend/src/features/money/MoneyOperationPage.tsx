import { useMutation, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { useAuth } from "react-oidc-context";
import { z } from "zod";

import { hasScope, parseScopes } from "../authentication/permissions";
import {
  moneyOperationRequestSchema,
  moneyOperationResponseSchema,
  type MoneyOperationRequest,
} from "../../shared/api/contracts";
import { BankingApiError } from "../../shared/api/BankingApiClient";
import { useBankingApi } from "../../shared/api/useBankingApi";
import { Button } from "../../shared/design-system/components/Button";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "../../shared/design-system/workspace.module.css";

type Operation = "deposit" | "withdrawal";
const accountIdSchema = z.uuid();

export function MoneyOperationPage() {
  const api = useBankingApi();
  const auth = useAuth();
  const queryClient = useQueryClient();
  const canWrite = hasScope(parseScopes(auth.user?.scope), "banking.write");
  const [accountId, setAccountId] = useState("");
  const [operation, setOperation] = useState<Operation>("deposit");
  const [amount, setAmount] = useState("");
  const [currencyCode, setCurrencyCode] = useState("EUR");
  const [description, setDescription] = useState("");
  const [reviewedRequest, setReviewedRequest] =
    useState<MoneyOperationRequest>();
  const [idempotencyKey, setIdempotencyKey] = useState<string>();
  const [message, setMessage] = useState<string>();

  const submitOperation = useMutation({
    mutationFn: async () => {
      if (!reviewedRequest || !idempotencyKey)
        throw new Error("Review is required.");
      return api.post(
        `/api/v1/accounts/${accountId}/${operation === "deposit" ? "deposits" : "withdrawals"}`,
        reviewedRequest,
        moneyOperationResponseSchema,
        { "Idempotency-Key": idempotencyKey },
      );
    },
    onSuccess: (receipt) => {
      setMessage("Operation completed and confirmed by the banking service.");
      void queryClient.invalidateQueries({
        queryKey: ["account", receipt.accountId],
      });
      void queryClient.invalidateQueries({
        queryKey: ["transactions", receipt.accountId],
      });
    },
  });

  function review(event: FormEvent) {
    event.preventDefault();
    const validAccount = accountIdSchema.safeParse(accountId.trim());
    const validRequest = moneyOperationRequestSchema.safeParse({
      amount: Number(amount),
      currencyCode: currencyCode.toUpperCase(),
      description: description.trim() || null,
    });
    if (!validAccount.success || !validRequest.success) {
      setMessage(
        validAccount.error?.issues[0]?.message ??
          validRequest.error?.issues[0]?.message ??
          "Check the operation details.",
      );
      return;
    }
    setAccountId(validAccount.data);
    setReviewedRequest(validRequest.data);
    setIdempotencyKey(crypto.randomUUID());
    setMessage(undefined);
    submitOperation.reset();
  }

  function reset() {
    setReviewedRequest(undefined);
    setIdempotencyKey(undefined);
    setMessage(undefined);
    submitOperation.reset();
  }

  const receipt = submitOperation.data;
  const ambiguousFailure =
    submitOperation.error &&
    !(submitOperation.error instanceof BankingApiError);

  return (
    <main>
      <h1>Deposit or withdraw</h1>
      <p>
        Review every operation before it is submitted to the banking ledger.
      </p>
      {!canWrite ? (
        <Surface>
          <p>You do not have permission to perform money operations.</p>
        </Surface>
      ) : (
        <div className={styles.layout}>
          <Surface aria-labelledby="operation-details">
            <h2 id="operation-details">Operation details</h2>
            <form className={styles.form} onSubmit={review}>
              <label>
                Operation
                <select
                  onChange={(event) =>
                    setOperation(event.target.value as Operation)
                  }
                  value={operation}
                >
                  <option value="deposit">Deposit</option>
                  <option value="withdrawal">Withdrawal</option>
                </select>
              </label>
              <label>
                Account identifier
                <input
                  onChange={(event) => setAccountId(event.target.value)}
                  value={accountId}
                />
              </label>
              <label>
                Amount
                <input
                  inputMode="decimal"
                  min="0.01"
                  onChange={(event) => setAmount(event.target.value)}
                  step="0.01"
                  type="number"
                  value={amount}
                />
              </label>
              <label>
                Currency
                <input
                  maxLength={3}
                  onChange={(event) =>
                    setCurrencyCode(event.target.value.toUpperCase())
                  }
                  value={currencyCode}
                />
              </label>
              <label>
                Description
                <input
                  maxLength={500}
                  onChange={(event) => setDescription(event.target.value)}
                  value={description}
                />
              </label>
              <Button type="submit">Review operation</Button>
            </form>
          </Surface>

          <Surface aria-labelledby="review-operation">
            <h2 id="review-operation">Review and confirmation</h2>
            {!reviewedRequest && (
              <p>Complete the form to review the operation.</p>
            )}
            {reviewedRequest && !receipt && (
              <div className={styles.details}>
                <strong>{operation.toUpperCase()}</strong>
                <span>
                  {reviewedRequest.amount.toFixed(2)}{" "}
                  {reviewedRequest.currencyCode}
                </span>
                <span className={styles.identifier}>Account {accountId}</span>
                <span className={styles.identifier}>
                  Request {idempotencyKey}
                </span>
                <div className={styles.actions}>
                  <Button
                    busy={submitOperation.isPending}
                    onClick={() => submitOperation.mutate()}
                  >
                    Confirm {operation}
                  </Button>
                  <Button onClick={reset} variant="secondary">
                    Change details
                  </Button>
                </div>
              </div>
            )}
            {receipt && (
              <div className={styles.details} role="status">
                <h3>Operation confirmed</h3>
                <span className={styles.identifier}>
                  Transaction {receipt.transactionId}
                </span>
                <span className={styles.identifier}>
                  Ledger entry {receipt.ledgerEntryId}
                </span>
                <strong>
                  New balance: {receipt.balance.toFixed(2)}{" "}
                  {receipt.currencyCode}
                </strong>
                <Button onClick={reset} variant="secondary">
                  Start another operation
                </Button>
              </div>
            )}
            {submitOperation.error && (
              <p role="alert">
                {ambiguousFailure
                  ? "The outcome is unknown. Retry with the same request key or verify transaction history."
                  : submitOperation.error.message}
              </p>
            )}
            {message && <p role="status">{message}</p>}
          </Surface>
        </div>
      )}
    </main>
  );
}

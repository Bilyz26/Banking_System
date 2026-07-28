import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { useAuth } from "react-oidc-context";
import { z } from "zod";

import { hasScope, parseScopes } from "../authentication/permissions";
import {
  accountSchema,
  accountStatusSchema,
  openAccountSchema,
  type OpenAccountRequest,
} from "../../shared/api/contracts";
import { useBankingApi } from "../../shared/api/useBankingApi";
import { Button } from "../../shared/design-system/components/Button";
import { ConfirmationDialog } from "../../shared/design-system/components/ConfirmationDialog";
import { StatusBadge } from "../../shared/design-system/components/StatusBadge";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "../../shared/design-system/workspace.module.css";

type LifecycleAction = "freeze" | "unfreeze" | "close";
const accountIdSchema = z.uuid("Enter a valid account UUID.");
const emptyRequest: OpenAccountRequest = { ownerId: "", currencyCode: "EUR" };

export function AccountPage() {
  const api = useBankingApi();
  const auth = useAuth();
  const queryClient = useQueryClient();
  const canAdminister = hasScope(
    parseScopes(auth.user?.scope),
    "banking.admin",
  );
  const [lookupInput, setLookupInput] = useState("");
  const [accountId, setAccountId] = useState<string>();
  const [openRequest, setOpenRequest] =
    useState<OpenAccountRequest>(emptyRequest);
  const [message, setMessage] = useState<string>();
  const [pendingAction, setPendingAction] = useState<LifecycleAction>();

  const accountQuery = useQuery({
    queryKey: ["account", accountId],
    queryFn: () => api.get(`/api/v1/accounts/${accountId}`, accountSchema),
    enabled: Boolean(accountId),
  });

  const openAccount = useMutation({
    mutationFn: (request: OpenAccountRequest) =>
      api.post("/api/v1/accounts", request, accountSchema),
    onSuccess: (account) => {
      setAccountId(account.accountId);
      setLookupInput(account.accountId);
      setMessage(`Account ${account.accountId} was opened.`);
      queryClient.setQueryData(["account", account.accountId], account);
    },
  });

  const lifecycle = useMutation({
    mutationFn: (action: LifecycleAction) =>
      api.post(
        `/api/v1/accounts/${accountId}/${action}`,
        undefined,
        accountStatusSchema,
      ),
    onSuccess: (result) => {
      queryClient.setQueryData(
        ["account", result.accountId],
        accountQuery.data
          ? { ...accountQuery.data, status: result.status }
          : undefined,
      );
      setMessage(`Account status changed to ${result.status}.`);
      setPendingAction(undefined);
    },
  });

  function lookup(event: FormEvent) {
    event.preventDefault();
    const result = accountIdSchema.safeParse(lookupInput.trim());
    setMessage(result.success ? undefined : result.error.issues[0]?.message);
    if (result.success) setAccountId(result.data);
  }

  function submitOpen(event: FormEvent) {
    event.preventDefault();
    const result = openAccountSchema.safeParse(openRequest);
    if (!result.success) {
      setMessage(result.error.issues[0]?.message);
      return;
    }
    openAccount.mutate(result.data);
  }

  const account = accountQuery.data;

  return (
    <main>
      <h1>Accounts</h1>
      <p>Retrieve an account, open a new account, or manage its lifecycle.</p>
      <div className={styles.layout}>
        <Surface aria-labelledby="account-lookup">
          <h2 id="account-lookup">Find account</h2>
          <form className={styles.form} onSubmit={lookup}>
            <label>
              Account identifier
              <input
                onChange={(event) => setLookupInput(event.target.value)}
                value={lookupInput}
              />
            </label>
            <Button busy={accountQuery.isFetching} type="submit">
              Retrieve account
            </Button>
          </form>
          {accountQuery.error && (
            <p role="alert">{accountQuery.error.message}</p>
          )}
          {account && (
            <div className={styles.details}>
              <StatusBadge
                tone={account.status === "ACTIVE" ? "success" : "warning"}
              >
                {account.status}
              </StatusBadge>
              <strong>
                {new Intl.NumberFormat(undefined, {
                  currency: account.currencyCode,
                  style: "currency",
                }).format(account.balance)}
              </strong>
              <span className={styles.identifier}>
                Account {account.accountId}
              </span>
              <span className={styles.identifier}>Owner {account.ownerId}</span>
              {canAdminister && account.status !== "CLOSED" && (
                <div className={styles.actions}>
                  <Button
                    onClick={() =>
                      setPendingAction(
                        account.status === "FROZEN" ? "unfreeze" : "freeze",
                      )
                    }
                    variant="secondary"
                  >
                    {account.status === "FROZEN" ? "Unfreeze" : "Freeze"}
                  </Button>
                  <Button
                    onClick={() => setPendingAction("close")}
                    variant="danger"
                  >
                    Close account
                  </Button>
                </div>
              )}
            </div>
          )}
        </Surface>

        <Surface aria-labelledby="open-account">
          <h2 id="open-account">Open account</h2>
          {!canAdminister ? (
            <p>You do not have permission to open accounts.</p>
          ) : (
            <form className={styles.form} onSubmit={submitOpen}>
              <label>
                Customer identifier
                <input
                  onChange={(event) =>
                    setOpenRequest((current) => ({
                      ...current,
                      ownerId: event.target.value,
                    }))
                  }
                  value={openRequest.ownerId}
                />
              </label>
              <label>
                Currency
                <input
                  maxLength={3}
                  onChange={(event) =>
                    setOpenRequest((current) => ({
                      ...current,
                      currencyCode: event.target.value.toUpperCase(),
                    }))
                  }
                  value={openRequest.currencyCode}
                />
              </label>
              <Button busy={openAccount.isPending} type="submit">
                Open account
              </Button>
            </form>
          )}
          {openAccount.error && <p role="alert">{openAccount.error.message}</p>}
          {lifecycle.error && <p role="alert">{lifecycle.error.message}</p>}
          {message && <p role="status">{message}</p>}
        </Surface>
      </div>

      {pendingAction && (
        <ConfirmationDialog
          busy={lifecycle.isPending}
          confirmLabel={
            pendingAction === "close"
              ? "Close account"
              : `${pendingAction} account`
          }
          description={`This will ${pendingAction} account ${accountId}. The API will enforce all balance and lifecycle rules.`}
          onCancel={() => setPendingAction(undefined)}
          onConfirm={() => lifecycle.mutate(pendingAction)}
          title="Confirm account change"
        />
      )}
    </main>
  );
}

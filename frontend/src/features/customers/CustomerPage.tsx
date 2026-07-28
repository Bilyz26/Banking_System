import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { type FormEvent, useState } from "react";
import { useAuth } from "react-oidc-context";
import { z } from "zod";

import { parseScopes, hasScope } from "../authentication/permissions";
import {
  customerProfileSchema,
  customerSchema,
  type CustomerProfile,
} from "../../shared/api/contracts";
import { useBankingApi } from "../../shared/api/useBankingApi";
import { Button } from "../../shared/design-system/components/Button";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "../../shared/design-system/workspace.module.css";

const customerIdSchema = z.uuid("Enter a valid customer UUID.");
const emptyProfile: CustomerProfile = { fullName: "", emailAddress: "" };

export function CustomerPage() {
  const api = useBankingApi();
  const auth = useAuth();
  const queryClient = useQueryClient();
  const scopes = parseScopes(auth.user?.scope);
  const canAdminister = hasScope(scopes, "banking.admin");
  const [lookupInput, setLookupInput] = useState("");
  const [customerId, setCustomerId] = useState<string>();
  const [profile, setProfile] = useState(emptyProfile);
  const [message, setMessage] = useState<string>();

  const customerQuery = useQuery({
    queryKey: ["customer", customerId],
    queryFn: () => api.get(`/api/v1/customers/${customerId}`, customerSchema),
    enabled: Boolean(customerId),
  });

  const createCustomer = useMutation({
    mutationFn: (request: CustomerProfile) =>
      api.post("/api/v1/customers", request, customerSchema),
    onSuccess: (customer) => {
      setCustomerId(customer.customerId);
      setLookupInput(customer.customerId);
      setMessage(`Customer ${customer.customerId} was created.`);
      queryClient.setQueryData(["customer", customer.customerId], customer);
    },
  });

  const updateCustomer = useMutation({
    mutationFn: (request: CustomerProfile) =>
      api.put(`/api/v1/customers/${customerId}`, request, customerSchema),
    onSuccess: (customer) => {
      setMessage("Customer profile was updated.");
      queryClient.setQueryData(["customer", customer.customerId], customer);
    },
  });

  function lookup(event: FormEvent) {
    event.preventDefault();
    const result = customerIdSchema.safeParse(lookupInput.trim());
    setMessage(result.success ? undefined : result.error.issues[0]?.message);
    if (result.success) setCustomerId(result.data);
  }

  function submitProfile(event: FormEvent) {
    event.preventDefault();
    const result = customerProfileSchema.safeParse(profile);
    if (!result.success) {
      setMessage(result.error.issues[0]?.message);
      return;
    }
    setMessage(undefined);
    if (customerId) updateCustomer.mutate(result.data);
    else createCustomer.mutate(result.data);
  }

  const displayedCustomer = customerQuery.data;

  return (
    <main>
      <h1>Customers</h1>
      <p>
        Retrieve a customer by UUID or create an authorized customer record.
      </p>
      <div className={styles.layout}>
        <Surface aria-labelledby="find-customer">
          <h2 id="find-customer">Find customer</h2>
          <form className={styles.form} onSubmit={lookup}>
            <label>
              Customer identifier
              <input
                onChange={(event) => setLookupInput(event.target.value)}
                value={lookupInput}
              />
            </label>
            <Button busy={customerQuery.isFetching} type="submit">
              Retrieve customer
            </Button>
          </form>
          {customerQuery.error && (
            <p role="alert">{customerQuery.error.message}</p>
          )}
          {displayedCustomer && (
            <div className={styles.details}>
              <h3>{displayedCustomer.fullName}</h3>
              <a href={`mailto:${displayedCustomer.emailAddress}`}>
                {displayedCustomer.emailAddress}
              </a>
              <span className={styles.identifier}>
                {displayedCustomer.customerId}
              </span>
              {canAdminister && (
                <Button
                  onClick={() =>
                    setProfile({
                      fullName: displayedCustomer.fullName,
                      emailAddress: displayedCustomer.emailAddress,
                    })
                  }
                  variant="secondary"
                >
                  Edit profile
                </Button>
              )}
            </div>
          )}
        </Surface>

        <Surface aria-labelledby="customer-form">
          <h2 id="customer-form">
            {customerId ? "Update customer" : "Create customer"}
          </h2>
          {!canAdminister ? (
            <p>You do not have permission to change customer records.</p>
          ) : (
            <form className={styles.form} onSubmit={submitProfile}>
              <label>
                Full name
                <input
                  maxLength={200}
                  onChange={(event) =>
                    setProfile((current) => ({
                      ...current,
                      fullName: event.target.value,
                    }))
                  }
                  value={profile.fullName}
                />
              </label>
              <label>
                Email address
                <input
                  maxLength={320}
                  onChange={(event) =>
                    setProfile((current) => ({
                      ...current,
                      emailAddress: event.target.value,
                    }))
                  }
                  type="email"
                  value={profile.emailAddress}
                />
              </label>
              <Button
                busy={createCustomer.isPending || updateCustomer.isPending}
                type="submit"
              >
                {customerId ? "Save profile" : "Create customer"}
              </Button>
              {customerId && (
                <Button
                  onClick={() => {
                    setCustomerId(undefined);
                    setProfile(emptyProfile);
                  }}
                  variant="quiet"
                >
                  Create another customer
                </Button>
              )}
            </form>
          )}
          {(createCustomer.error || updateCustomer.error) && (
            <p role="alert">
              {(createCustomer.error ?? updateCustomer.error)?.message}
            </p>
          )}
          {message && <p role="status">{message}</p>}
        </Surface>
      </div>
    </main>
  );
}

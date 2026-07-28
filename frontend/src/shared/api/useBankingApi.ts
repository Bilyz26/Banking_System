import { useMemo } from "react";
import { useAuth } from "react-oidc-context";

import { readEnvironment } from "../config/environment";
import { BankingApiClient } from "./BankingApiClient";

const environment = readEnvironment(import.meta.env);

export function useBankingApi(): BankingApiClient {
  const authentication = useAuth();
  const accessToken = authentication.user?.access_token;
  const api = useMemo(
    () =>
      new BankingApiClient(environment.VITE_API_BASE_URL, accessToken ?? ""),
    [accessToken],
  );

  if (!accessToken) {
    throw new Error("An authenticated access token is required.");
  }

  return api;
}

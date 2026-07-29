import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { type PropsWithChildren, useState } from "react";
import { AuthProvider } from "react-oidc-context";

import { AuthenticationGate } from "../features/authentication/AuthenticationGate";
import { createAuthConfig } from "../features/authentication/authConfig";
import { readEnvironment } from "../shared/config/environment";

const environment = readEnvironment(import.meta.env);
const authConfig = createAuthConfig(environment);

export function ApplicationProviders({ children }: PropsWithChildren) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: 1,
            staleTime: 30_000,
          },
          mutations: {
            retry: false,
          },
        },
      }),
  );

  return (
    <AuthProvider {...authConfig}>
      <AuthenticationGate>
        <QueryClientProvider client={queryClient}>
          {children}
        </QueryClientProvider>
      </AuthenticationGate>
    </AuthProvider>
  );
}

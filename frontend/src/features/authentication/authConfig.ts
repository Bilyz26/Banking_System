import { WebStorageStateStore } from "oidc-client-ts";
import type { AuthProviderProps } from "react-oidc-context";

import type { Environment } from "../../shared/config/environment";

export function createAuthConfig(
  environment: Environment,
  origin = window.location.origin,
): AuthProviderProps {
  return {
    authority: environment.VITE_OIDC_ISSUER_URL,
    client_id: environment.VITE_OIDC_CLIENT_ID,
    redirect_uri: `${origin}/dashboard`,
    post_logout_redirect_uri: origin,
    response_type: "code",
    scope: "openid profile email banking.read banking.write banking.admin",
    automaticSilentRenew: true,
    monitorSession: true,
    stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
    userStore: new WebStorageStateStore({ store: window.sessionStorage }),
    onSigninCallback: () => {
      window.history.replaceState({}, document.title, window.location.pathname);
    },
  };
}

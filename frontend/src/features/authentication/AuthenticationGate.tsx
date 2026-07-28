import type { PropsWithChildren } from "react";
import { useAuth } from "react-oidc-context";

import { Button } from "../../shared/design-system/components/Button";
import { Surface } from "../../shared/design-system/components/Surface";
import styles from "./AuthenticationGate.module.css";

export function AuthenticationGate({ children }: PropsWithChildren) {
  const authentication = useAuth();

  if (authentication.activeNavigator || authentication.isLoading) {
    return <main className={styles.center}>Preparing secure workspace…</main>;
  }

  if (authentication.error) {
    return (
      <main className={styles.center}>
        <Surface aria-labelledby="authentication-error">
          <h1 id="authentication-error">Sign-in could not be completed</h1>
          <p>{authentication.error.message}</p>
          <Button onClick={() => void authentication.signinRedirect()}>
            Try sign in again
          </Button>
        </Surface>
      </main>
    );
  }

  if (!authentication.isAuthenticated) {
    return (
      <main className={styles.center}>
        <Surface aria-labelledby="sign-in-title">
          <p className={styles.eyebrow}>Secure operations portal</p>
          <h1 id="sign-in-title">Welcome to Banking System</h1>
          <p>Sign in with your authorized banking workspace account.</p>
          <Button onClick={() => void authentication.signinRedirect()}>
            Sign in securely
          </Button>
        </Surface>
      </main>
    );
  }

  return children;
}

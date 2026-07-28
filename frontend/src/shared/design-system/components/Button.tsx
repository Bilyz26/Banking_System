import type { ButtonHTMLAttributes, PropsWithChildren } from "react";

import styles from "./Button.module.css";

type ButtonVariant = "primary" | "secondary" | "danger" | "quiet";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  busy?: boolean;
}

export function Button({
  busy = false,
  children,
  disabled,
  type = "button",
  variant = "primary",
  ...properties
}: PropsWithChildren<ButtonProps>) {
  return (
    <button
      {...properties}
      aria-busy={busy || undefined}
      className={`${styles.button} ${styles[variant]}`}
      disabled={disabled || busy}
      type={type}
    >
      {busy ? <span aria-hidden="true">Please wait</span> : children}
      {busy && <span className={styles.hidden}>{children}</span>}
    </button>
  );
}

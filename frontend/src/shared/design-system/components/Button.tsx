import {
  forwardRef,
  type ButtonHTMLAttributes,
  type PropsWithChildren,
} from "react";

import styles from "./Button.module.css";

type ButtonVariant = "primary" | "secondary" | "danger" | "quiet";

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  busy?: boolean;
}

export const Button = forwardRef<
  HTMLButtonElement,
  PropsWithChildren<ButtonProps>
>(function Button(
  {
    busy = false,
    children,
    disabled,
    type = "button",
    variant = "primary",
    ...properties
  },
  ref,
) {
  return (
    <button
      {...properties}
      ref={ref}
      aria-busy={busy || undefined}
      className={`${styles.button} ${styles[variant]}`}
      disabled={disabled || busy}
      type={type}
    >
      {busy ? <span aria-hidden="true">Please wait</span> : children}
      {busy && <span className={styles.hidden}>{children}</span>}
    </button>
  );
});

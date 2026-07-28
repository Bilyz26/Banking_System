import type { HTMLAttributes, PropsWithChildren } from "react";

import styles from "./Surface.module.css";

export function Surface({
  children,
  className,
  ...properties
}: PropsWithChildren<HTMLAttributes<HTMLElement>>) {
  return (
    <section
      className={[styles.surface, className].filter(Boolean).join(" ")}
      {...properties}
    >
      {children}
    </section>
  );
}

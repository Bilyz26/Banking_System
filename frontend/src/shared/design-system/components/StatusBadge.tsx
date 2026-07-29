import type { PropsWithChildren } from "react";

import styles from "./StatusBadge.module.css";

type Tone = "neutral" | "success" | "warning" | "danger";

export function StatusBadge({
  children,
  tone = "neutral",
}: PropsWithChildren<{ tone?: Tone }>) {
  return <span className={`${styles.badge} ${styles[tone]}`}>{children}</span>;
}

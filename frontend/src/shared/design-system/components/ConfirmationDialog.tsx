import { useEffect, useRef } from "react";

import { Button } from "./Button";
import styles from "./ConfirmationDialog.module.css";

interface ConfirmationDialogProps {
  title: string;
  description: string;
  confirmLabel: string;
  busy?: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export function ConfirmationDialog({
  title,
  description,
  confirmLabel,
  busy = false,
  onCancel,
  onConfirm,
}: ConfirmationDialogProps) {
  const cancelButton = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    cancelButton.current?.focus();
  }, []);

  return (
    <div
      className={styles.backdrop}
      onKeyDown={(event) => {
        if (event.key === "Escape" && !busy) onCancel();
      }}
    >
      <section
        aria-describedby="confirmation-description"
        aria-labelledby="confirmation-title"
        aria-modal="true"
        className={styles.dialog}
        role="alertdialog"
      >
        <h2 id="confirmation-title">{title}</h2>
        <p id="confirmation-description">{description}</p>
        <div className={styles.actions}>
          <Button onClick={onCancel} ref={cancelButton} variant="secondary">
            Cancel
          </Button>
          <Button busy={busy} onClick={onConfirm} variant="danger">
            {confirmLabel}
          </Button>
        </div>
      </section>
    </div>
  );
}

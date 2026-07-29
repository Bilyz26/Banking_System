import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { ConfirmationDialog } from "./ConfirmationDialog";

describe("ConfirmationDialog", () => {
  it("focuses the safe action and supports Escape", async () => {
    const cancel = vi.fn();
    const user = userEvent.setup();
    render(
      <ConfirmationDialog
        confirmLabel="Close account"
        description="This action changes account state."
        onCancel={cancel}
        onConfirm={vi.fn()}
        title="Confirm change"
      />,
    );

    expect(screen.getByRole("button", { name: "Cancel" })).toHaveFocus();
    await user.keyboard("{Escape}");
    expect(cancel).toHaveBeenCalledOnce();
  });

  it("keeps keyboard focus inside the dialog", async () => {
    const user = userEvent.setup();
    render(
      <ConfirmationDialog
        confirmLabel="Confirm"
        description="Review this action."
        onCancel={vi.fn()}
        onConfirm={vi.fn()}
        title="Confirm action"
      />,
    );
    await user.keyboard("{Shift>}{Tab}{/Shift}");
    expect(screen.getByRole("button", { name: "Confirm" })).toHaveFocus();
    await user.keyboard("{Tab}");
    expect(screen.getByRole("button", { name: "Cancel" })).toHaveFocus();
  });
});

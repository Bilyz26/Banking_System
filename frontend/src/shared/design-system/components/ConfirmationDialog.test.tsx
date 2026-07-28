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
});

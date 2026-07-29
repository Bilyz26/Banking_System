import axe from "axe-core";
import { render } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { Button } from "./components/Button";
import { ConfirmationDialog } from "./components/ConfirmationDialog";
import { StatusBadge } from "./components/StatusBadge";
import { Surface } from "./components/Surface";

describe("design system accessibility", () => {
  it("has no automatically detectable WCAG violations", async () => {
    const { container } = render(
      <>
        <main>
          <h1>Account workspace</h1>
          <Surface aria-labelledby="account-title">
            <h2 id="account-title">Account</h2>
            <StatusBadge tone="success">Active</StatusBadge>
            <Button>Continue</Button>
          </Surface>
        </main>
        <ConfirmationDialog
          confirmLabel="Close account"
          description="The account will be closed."
          onCancel={vi.fn()}
          onConfirm={vi.fn()}
          title="Confirm account closure"
        />
      </>,
    );

    const results = await axe.run(container, {
      runOnly: { type: "tag", values: ["wcag2a", "wcag2aa", "wcag21aa"] },
      rules: { "color-contrast": { enabled: false } },
    });
    expect(results.violations).toEqual([]);
  });
});

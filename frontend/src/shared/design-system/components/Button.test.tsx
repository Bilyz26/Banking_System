import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { Button } from "./Button";

describe("Button", () => {
  it("invokes its action once", async () => {
    const action = vi.fn();
    const user = userEvent.setup();
    render(<Button onClick={action}>Transfer</Button>);
    await user.click(screen.getByRole("button", { name: "Transfer" }));
    expect(action).toHaveBeenCalledOnce();
  });

  it("prevents interaction while busy", () => {
    render(<Button busy>Confirm transfer</Button>);
    const button = screen.getByRole("button", { name: "Confirm transfer" });
    expect(button).toBeDisabled();
    expect(button).toHaveAttribute("aria-busy", "true");
  });
});

import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { Application } from "./Application";

describe("AppShell", () => {
  it("provides primary and mobile navigation", async () => {
    window.history.pushState({}, "", "/dashboard");
    render(<Application />);

    expect(
      await screen.findByRole("navigation", { name: "Primary navigation" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("navigation", { name: "Mobile navigation" }),
    ).toBeInTheDocument();
    expect(
      screen.getAllByRole("link", { name: "Dashboard" })[0],
    ).toHaveAttribute("aria-current", "page");
  });
});

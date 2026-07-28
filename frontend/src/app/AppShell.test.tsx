import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { Application } from "./Application";

vi.mock("react-oidc-context", () => ({
  useAuth: () => ({
    user: {
      access_token: "test-access-token",
      profile: { name: "Test Operator" },
    },
    signoutRedirect: vi.fn(),
  }),
}));

describe("AppShell", () => {
  it("provides primary and mobile navigation", async () => {
    window.history.pushState({}, "", "/dashboard");
    render(
      <QueryClientProvider client={new QueryClient()}>
        <Application />
      </QueryClientProvider>,
    );

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

import { expect, test } from "@playwright/test";

test("protects the banking workspace behind sign in", async ({ page }) => {
  await page.goto("/dashboard");

  await expect(
    page.getByRole("heading", { name: "Welcome to Banking System" }),
  ).toBeVisible();
  await expect(
    page.getByRole("button", { name: "Sign in securely" }),
  ).toBeVisible();
  await expect(
    page.getByRole("navigation", { name: "Primary navigation" }),
  ).not.toBeVisible();
});

test("keeps the authentication boundary usable on mobile", async ({
  page,
}, testInfo) => {
  test.skip(!testInfo.project.name.includes("mobile"), "Mobile project only");
  await page.goto("/");

  const signIn = page.getByRole("button", { name: "Sign in securely" });
  await expect(signIn).toBeVisible();
  await expect(signIn).toBeInViewport();
});

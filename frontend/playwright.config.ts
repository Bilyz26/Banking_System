import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: "http://127.0.0.1:4173",
    trace: "on-first-retry",
  },
  projects: [
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
    { name: "mobile-chromium", use: { ...devices["Pixel 7"] } },
  ],
  webServer: {
    command: "npm run dev -- --host 127.0.0.1 --port 4173",
    env: {
      VITE_API_BASE_URL: "/backend",
      VITE_OIDC_ISSUER_URL: "http://localhost:9000/realms/banking",
      VITE_OIDC_CLIENT_ID: "banking-web",
    },
    reuseExistingServer: !process.env.CI,
    url: "http://127.0.0.1:4173",
  },
});

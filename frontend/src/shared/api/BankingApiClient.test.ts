import { describe, expect, it, vi } from "vitest";

import { accountSchema } from "./contracts";
import { BankingApiClient, BankingApiError } from "./BankingApiClient";

const account = {
  accountId: "f07f7fe1-5740-4d37-b4c9-1435d4487136",
  ownerId: "25c20494-4eda-47e0-a99f-f9f48c2ac1fa",
  balance: 1250,
  currencyCode: "EUR",
  status: "ACTIVE",
};

describe("BankingApiClient", () => {
  it("attaches the bearer token and validates a response", async () => {
    const request = vi.fn<typeof fetch>().mockResolvedValue(
      new Response(JSON.stringify(account), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    const client = new BankingApiClient("/backend", "token", request);

    await expect(
      client.get("/api/v1/accounts/1", accountSchema),
    ).resolves.toEqual(account);
    const [url, options] = request.mock.calls[0] ?? [];
    expect(url).toBe("/backend/api/v1/accounts/1");
    expect(new Headers(options?.headers).get("Authorization")).toBe(
      "Bearer token",
    );
  });

  it("surfaces structured API failures", async () => {
    const request = vi.fn<typeof fetch>().mockResolvedValue(
      new Response(
        JSON.stringify({
          timestamp: "2026-07-28T12:00:00Z",
          status: 404,
          code: "ACCOUNT_NOT_FOUND",
          message: "Account was not found.",
          fieldErrors: {},
        }),
        { status: 404 },
      ),
    );
    const client = new BankingApiClient("/backend", "token", request);

    await expect(client.get("/missing", accountSchema)).rejects.toEqual(
      expect.objectContaining<Partial<BankingApiError>>({
        status: 404,
        code: "ACCOUNT_NOT_FOUND",
      }),
    );
  });

  it("sends idempotency headers with financial requests", async () => {
    const request = vi
      .fn<typeof fetch>()
      .mockResolvedValue(
        new Response(JSON.stringify(account), { status: 200 }),
      );
    const client = new BankingApiClient("/backend", "token", request);

    await client.post("/operation", { amount: 10 }, accountSchema, {
      "Idempotency-Key": "93c937a8-a1f1-4c98-ac2b-4a0430d43575",
    });

    const [, options] = request.mock.calls[0] ?? [];
    expect(new Headers(options?.headers).get("Idempotency-Key")).toBe(
      "93c937a8-a1f1-4c98-ac2b-4a0430d43575",
    );
  });
});

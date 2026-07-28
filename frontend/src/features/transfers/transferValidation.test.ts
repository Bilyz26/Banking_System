import { describe, expect, it } from "vitest";

import { transferRequestSchema } from "../../shared/api/contracts";

describe("transfer validation", () => {
  it("rejects a transfer to the source account", () => {
    const accountId = "370056c5-24a2-4ed9-9bf8-d19c0ecb1ac9";
    const result = transferRequestSchema.safeParse({
      sourceAccountId: accountId,
      destinationAccountId: accountId,
      amount: 10,
      currencyCode: "EUR",
    });

    expect(result.success).toBe(false);
  });
});

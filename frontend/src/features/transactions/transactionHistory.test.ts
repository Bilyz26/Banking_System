import { describe, expect, it } from "vitest";

import { buildTransactionHistoryPath } from "./transactionHistoryPath";

describe("transaction history pagination", () => {
  it("passes an opaque cursor without parsing it", () => {
    const cursor = "opaque:value/with+symbols==";
    const path = buildTransactionHistoryPath("account", cursor);
    const url = new URL(path, "https://banking.test");

    expect(url.searchParams.get("cursor")).toBe(cursor);
    expect(url.searchParams.get("limit")).toBe("20");
  });
});

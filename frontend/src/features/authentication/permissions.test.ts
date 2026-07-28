import { describe, expect, it } from "vitest";

import { hasScope, parseScopes } from "./permissions";

describe("authentication permissions", () => {
  it("parses and recognizes an issued banking scope", () => {
    const scopes = parseScopes("openid banking.read banking.write");
    expect(hasScope(scopes, "banking.read")).toBe(true);
    expect(hasScope(scopes, "banking.admin")).toBe(false);
  });

  it("handles a missing scope claim safely", () => {
    expect(parseScopes(undefined).size).toBe(0);
  });
});

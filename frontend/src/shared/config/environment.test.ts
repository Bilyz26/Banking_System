import { describe, expect, it } from "vitest";

import { readEnvironment } from "./environment";

describe("readEnvironment", () => {
  it("accepts complete frontend configuration", () => {
    expect(
      readEnvironment({
        VITE_API_BASE_URL: "/backend",
        VITE_OIDC_ISSUER_URL: "http://localhost:9000/realms/banking",
        VITE_OIDC_CLIENT_ID: "banking-web",
      }),
    ).toEqual({
      VITE_API_BASE_URL: "/backend",
      VITE_OIDC_ISSUER_URL: "http://localhost:9000/realms/banking",
      VITE_OIDC_CLIENT_ID: "banking-web",
    });
  });

  it("rejects incomplete frontend configuration", () => {
    expect(() => readEnvironment({})).toThrow();
  });
});

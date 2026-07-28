import type { ZodType } from "zod";

import { apiErrorSchema } from "./contracts";

export class BankingApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code: string,
    readonly correlationId?: string,
  ) {
    super(message);
    this.name = "BankingApiError";
  }
}

export class BankingApiClient {
  constructor(
    private readonly baseUrl: string,
    private readonly accessToken: string,
    private readonly request: typeof fetch = fetch,
  ) {}

  async get<T>(path: string, schema: ZodType<T>): Promise<T> {
    const response = await this.request(`${this.baseUrl}${path}`, {
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${this.accessToken}`,
      },
    });

    if (!response.ok) {
      const problem = apiErrorSchema.safeParse(await response.json());
      throw new BankingApiError(
        problem.success
          ? problem.data.message
          : "The banking service rejected the request.",
        response.status,
        problem.success ? problem.data.code : "UNEXPECTED_RESPONSE",
        response.headers.get("X-Correlation-ID") ?? undefined,
      );
    }

    return schema.parse(await response.json());
  }
}

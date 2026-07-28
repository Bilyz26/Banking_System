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
    return this.send("GET", path, schema);
  }

  async post<T>(path: string, body: unknown, schema: ZodType<T>): Promise<T> {
    return this.send("POST", path, schema, body);
  }

  async put<T>(path: string, body: unknown, schema: ZodType<T>): Promise<T> {
    return this.send("PUT", path, schema, body);
  }

  private async send<T>(
    method: "GET" | "POST" | "PUT",
    path: string,
    schema: ZodType<T>,
    body?: unknown,
  ): Promise<T> {
    const response = await this.request(`${this.baseUrl}${path}`, {
      method,
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${this.accessToken}`,
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
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

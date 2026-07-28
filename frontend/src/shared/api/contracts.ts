import { z } from "zod";

export const accountSchema = z.object({
  accountId: z.uuid(),
  ownerId: z.uuid(),
  balance: z.number(),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
  status: z.enum(["ACTIVE", "FROZEN", "CLOSED"]),
});

export const transactionSchema = z.object({
  ledgerEntryId: z.uuid(),
  transactionId: z.uuid(),
  type: z.enum(["DEPOSIT", "WITHDRAWAL", "TRANSFER_DEBIT", "TRANSFER_CREDIT"]),
  amount: z.number().positive(),
  balanceAfter: z.number(),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
  occurredAt: z.iso.datetime(),
  description: z.string().nullable().optional(),
});

export const transactionPageSchema = z.object({
  transactions: z.array(transactionSchema),
  nextCursor: z.string().nullable().optional(),
});

export const apiErrorSchema = z.object({
  timestamp: z.iso.datetime(),
  status: z.number().int(),
  code: z.string(),
  message: z.string(),
  fieldErrors: z.record(z.string(), z.string()),
});

export type Account = z.infer<typeof accountSchema>;
export type Transaction = z.infer<typeof transactionSchema>;
export type TransactionPage = z.infer<typeof transactionPageSchema>;

export const customerProfileSchema = z.object({
  fullName: z.string().trim().min(1).max(200),
  emailAddress: z.email().max(320),
});

export const customerSchema = customerProfileSchema.extend({
  customerId: z.uuid(),
});

export type Customer = z.infer<typeof customerSchema>;
export type CustomerProfile = z.infer<typeof customerProfileSchema>;

export const openAccountSchema = z.object({
  ownerId: z.uuid(),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
});

export const accountStatusSchema = z.object({
  accountId: z.uuid(),
  status: z.enum(["ACTIVE", "FROZEN", "CLOSED"]),
});

export type OpenAccountRequest = z.infer<typeof openAccountSchema>;

export const moneyOperationRequestSchema = z.object({
  amount: z.number().positive().multipleOf(0.01),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
  description: z.string().max(500).nullable().optional(),
});

export const moneyOperationResponseSchema = z.object({
  accountId: z.uuid(),
  ledgerEntryId: z.uuid(),
  transactionId: z.uuid(),
  balance: z.number(),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
});

export type MoneyOperationRequest = z.infer<typeof moneyOperationRequestSchema>;
export type MoneyOperationResponse = z.infer<
  typeof moneyOperationResponseSchema
>;

export const transferRequestSchema = moneyOperationRequestSchema
  .extend({
    sourceAccountId: z.uuid(),
    destinationAccountId: z.uuid(),
  })
  .refine(
    (request) => request.sourceAccountId !== request.destinationAccountId,
    {
      message: "Source and destination accounts must be different.",
      path: ["destinationAccountId"],
    },
  );

export const transferResponseSchema = z.object({
  transactionId: z.uuid(),
  sourceAccountId: z.uuid(),
  sourceBalance: z.number(),
  debitEntryId: z.uuid(),
  destinationAccountId: z.uuid(),
  destinationBalance: z.number(),
  creditEntryId: z.uuid(),
  currencyCode: z.string().regex(/^[A-Z]{3}$/),
});

export type TransferRequest = z.infer<typeof transferRequestSchema>;

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

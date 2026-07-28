export function buildTransactionHistoryPath(
  accountId: string,
  cursor?: string,
): string {
  const query = new URLSearchParams({ limit: "20" });
  if (cursor) query.set("cursor", cursor);
  return `/api/v1/accounts/${accountId}/transactions?${query.toString()}`;
}

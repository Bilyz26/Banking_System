export type BankingScope =
  "banking.read" | "banking.write" | "banking.admin" | "banking.monitor";

export function parseScopes(scope: string | undefined): ReadonlySet<string> {
  return new Set(scope?.split(/\s+/).filter(Boolean) ?? []);
}

export function hasScope(
  grantedScopes: ReadonlySet<string>,
  requiredScope: BankingScope,
): boolean {
  return grantedScopes.has(requiredScope);
}

import { z } from "zod";

const environmentSchema = z.object({
  VITE_API_BASE_URL: z.union([
    z.url(),
    z.string().regex(/^\/[a-zA-Z0-9/_-]*$/),
  ]),
  VITE_OIDC_ISSUER_URL: z.url(),
  VITE_OIDC_CLIENT_ID: z.string().trim().min(1),
});

export type Environment = z.infer<typeof environmentSchema>;

export function readEnvironment(source: Record<string, unknown>): Environment {
  return environmentSchema.parse(source);
}

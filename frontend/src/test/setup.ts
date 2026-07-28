import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach, vi } from "vitest";

afterEach(cleanup);

vi.stubEnv("VITE_API_BASE_URL", "/backend");
vi.stubEnv("VITE_OIDC_ISSUER_URL", "http://localhost:9000/realms/banking");
vi.stubEnv("VITE_OIDC_CLIENT_ID", "banking-web");

import {
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
} from "@tanstack/react-router";

import { AppShell } from "./AppShell";
import { DashboardPage } from "../features/dashboard/DashboardPage";
import { NotFoundPage } from "../shared/components/NotFoundPage";
import { PlaceholderPage } from "../shared/components/PlaceholderPage";

const rootRoute = createRootRoute({
  component: AppShell,
  notFoundComponent: NotFoundPage,
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: DashboardPage,
});

const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/dashboard",
  component: DashboardPage,
});

const customersRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/customers",
  component: () => <PlaceholderPage title="Customers" />,
});

const accountsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/accounts",
  component: () => <PlaceholderPage title="Accounts" />,
});

const moneyRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/money",
  component: () => <PlaceholderPage title="Money operations" />,
});

const transactionsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/transactions",
  component: () => <PlaceholderPage title="Transactions" />,
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  dashboardRoute,
  customersRoute,
  accountsRoute,
  moneyRoute,
  transactionsRoute,
]);
const router = createRouter({ routeTree });

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

export function Application() {
  return <RouterProvider router={router} />;
}

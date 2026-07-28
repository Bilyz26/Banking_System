import {
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
} from "@tanstack/react-router";
import { lazy, Suspense, type ComponentType } from "react";

import { AppShell } from "./AppShell";
import { NotFoundPage } from "../shared/components/NotFoundPage";

function lazyNamedPage<TModule, TExport extends keyof TModule>(
  importer: () => Promise<TModule>,
  exportName: TExport,
) {
  return lazy(async () => ({
    default: (await importer())[exportName] as ComponentType,
  }));
}

const DashboardPage = lazyNamedPage(
  () => import("../features/dashboard/DashboardPage"),
  "DashboardPage",
);
const CustomerPage = lazyNamedPage(
  () => import("../features/customers/CustomerPage"),
  "CustomerPage",
);
const AccountPage = lazyNamedPage(
  () => import("../features/accounts/AccountPage"),
  "AccountPage",
);
const MoneyOperationPage = lazyNamedPage(
  () => import("../features/money/MoneyOperationPage"),
  "MoneyOperationPage",
);
const TransferPage = lazyNamedPage(
  () => import("../features/transfers/TransferPage"),
  "TransferPage",
);
const TransactionHistoryPage = lazyNamedPage(
  () => import("../features/transactions/TransactionHistoryPage"),
  "TransactionHistoryPage",
);

function renderLazyPage(Page: ComponentType) {
  return function LazyPageRoute() {
    return (
      <Suspense
        fallback={
          <p aria-live="polite" role="status">
            Loading page…
          </p>
        }
      >
        <Page />
      </Suspense>
    );
  };
}

const rootRoute = createRootRoute({
  component: AppShell,
  notFoundComponent: NotFoundPage,
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: renderLazyPage(DashboardPage),
});

const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/dashboard",
  component: renderLazyPage(DashboardPage),
});

const customersRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/customers",
  component: renderLazyPage(CustomerPage),
});

const accountsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/accounts",
  component: renderLazyPage(AccountPage),
});

const moneyRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/money",
  component: renderLazyPage(MoneyOperationPage),
});

const transactionsRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/transactions",
  component: renderLazyPage(TransactionHistoryPage),
});

const transfersRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/transfers",
  component: renderLazyPage(TransferPage),
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  dashboardRoute,
  customersRoute,
  accountsRoute,
  moneyRoute,
  transfersRoute,
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

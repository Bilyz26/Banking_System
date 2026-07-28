import { Link, Outlet, useRouterState } from "@tanstack/react-router";
import { useAuth } from "react-oidc-context";

import styles from "./AppShell.module.css";

const navigation = [
  { label: "Dashboard", path: "/dashboard", icon: "D" },
  { label: "Customers", path: "/customers", icon: "C" },
  { label: "Accounts", path: "/accounts", icon: "A" },
  { label: "Money", path: "/money", icon: "M" },
  { label: "Transfers", path: "/transfers", icon: "X" },
  { label: "Transactions", path: "/transactions", icon: "T" },
] as const;

export function AppShell() {
  const authentication = useAuth();
  const currentPath = useRouterState({
    select: (state) => state.location.pathname,
  });

  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar}>
        <Link
          className={styles.brand}
          to="/dashboard"
          aria-label="Banking home"
        >
          <span className={styles.brandMark} aria-hidden="true">
            B
          </span>
          <span>Banking System</span>
        </Link>

        <nav className={styles.navigation} aria-label="Primary navigation">
          {navigation.map((item) => {
            const active = currentPath.startsWith(item.path);
            return (
              <Link
                aria-current={active ? "page" : undefined}
                className={`${styles.navigationLink} ${active ? styles.active : ""}`}
                key={item.path}
                to={item.path}
              >
                <span className={styles.navigationIcon} aria-hidden="true">
                  {item.icon}
                </span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className={styles.securityNote}>
          <span className={styles.statusDot} aria-hidden="true" />
          <span>Secure workspace</span>
        </div>
      </aside>

      <div className={styles.workspace}>
        <header className={styles.header}>
          <div>
            <span className={styles.eyebrow}>Operations portal</span>
            <p className={styles.context}>Authenticated banking workspace</p>
          </div>
          <button
            className={styles.userMenu}
            onClick={() => void authentication.signoutRedirect()}
            type="button"
          >
            <span className={styles.avatar} aria-hidden="true">
              BA
            </span>
            <span className={styles.userDetails}>
              <strong>
                {authentication.user?.profile.name ?? "Bank operator"}
              </strong>
              <small>Sign out</small>
            </span>
          </button>
        </header>

        <div className={styles.content}>
          <Outlet />
        </div>
      </div>

      <nav className={styles.mobileNavigation} aria-label="Mobile navigation">
        {navigation.slice(0, 4).map((item) => {
          const active = currentPath.startsWith(item.path);
          return (
            <Link
              aria-current={active ? "page" : undefined}
              className={active ? styles.mobileActive : undefined}
              key={item.path}
              to={item.path}
            >
              <span aria-hidden="true">{item.icon}</span>
              <small>{item.label}</small>
            </Link>
          );
        })}
      </nav>
    </div>
  );
}

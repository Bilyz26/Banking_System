import { StatusBadge } from "../../shared/design-system/components/StatusBadge";
import { Surface } from "../../shared/design-system/components/Surface";

export function DashboardPage() {
  return (
    <main>
      <StatusBadge tone="success">Workspace available</StatusBadge>
      <h1>Banking workspace</h1>
      <Surface aria-labelledby="getting-started">
        <h2 id="getting-started">Start with an account</h2>
        <p>
          Retrieve an account to view its verified balance and transactions.
        </p>
      </Surface>
    </main>
  );
}

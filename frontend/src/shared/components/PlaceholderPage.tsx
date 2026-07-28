import { Surface } from "../design-system/components/Surface";

export function PlaceholderPage({ title }: { title: string }) {
  return (
    <main>
      <h1>{title}</h1>
      <Surface aria-label={`${title} workspace`}>
        <p>This workspace will be connected to the banking API next.</p>
      </Surface>
    </main>
  );
}

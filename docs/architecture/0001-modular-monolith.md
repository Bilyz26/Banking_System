# ADR 0001: Use a feature-oriented modular monolith

- Status: Accepted
- Date: 2026-07-23

## Context

The banking system needs strong business boundaries, testable rules, and atomic
operations. Starting with independently deployed services would add network,
deployment, and distributed-transaction complexity before those costs are
justified.

## Decision

Build one Spring Boot application organized by business feature. Each feature
uses four internal boundaries:

- `domain`: framework-independent business rules and models
- `application`: use cases and ports
- `infrastructure`: technical adapter implementations
- `presentation`: REST API adapters

Application assembly belongs to `bootstrap`. Only concepts that are genuinely
shared by multiple features belong to `shared`.

## Consequences

- Business rules can be tested without starting Spring or a database.
- Features remain understandable and independently evolvable.
- Transactions are simpler to make atomic.
- Package boundaries require deliberate enforcement until automated
  architecture tests are introduced.
- A feature can later be extracted into a service if operational needs justify it.


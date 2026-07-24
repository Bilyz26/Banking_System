# Banking System Delivery Roadmap

This document is the authoritative delivery roadmap for Banking System version
1.0.0. Phase numbers describe product scope, not branch or pull-request history.
Historical branches may use different phase numbers because implementation
temporarily drifted from the original plan.

## Status definitions

- **Complete**: the intended outcome is implemented and verified.
- **In progress**: useful parts exist, but the phase still has required work.
- **Planned**: the phase has not yet been completed.

Changing a phase's scope or accepting a technology substitution requires an
Architecture Decision Record (ADR). A phase may be marked complete only after
its tests and documentation are updated.

## Phases

| Phase | Outcome | Status | Completion notes |
|---:|---|---|---|
| 1 | Repair Development Toolchain and Run All Tests | Complete | Java 21, Maven Wrapper, and repeatable verification are available. |
| 2 | Finalize and Stabilize the Core Domain | Complete | Core customer, account, and money rules are framework-independent and tested. |
| 3 | Design the Transaction Ledger and Audit Model | Complete | Immutable ledger entries and paired transfer records are implemented. |
| 4 | Complete Customer Application Use Cases | Complete | Create, retrieve, and update-profile use cases are implemented. |
| 5 | Complete Account Application Use Cases | Complete | Account opening, retrieval, and lifecycle operations are implemented. |
| 6 | Complete Deposit and Withdrawal Use Cases | Complete | Validated, ledger-backed money operations are implemented. |
| 7 | Complete Transfer and Transaction-History Use Cases | Complete | Atomic transfers and cursor-paginated history are implemented. |
| 8 | Integrate PostgreSQL, JPA, and Flyway | Complete | PostgreSQL and Flyway use explicit JDBC; ADR 0007 accepts JDBC as the JPA substitution. |
| 9 | Add Atomic Transactions and Concurrency Protection | Complete | Database transactions, optimistic locking, and idempotency protection are implemented. |
| 10 | Complete Customer REST Endpoints | Complete | Create, retrieve, and update-profile endpoints are implemented and documented. |
| 11 | Complete Account REST Endpoints | Complete | Open, retrieve, freeze, unfreeze, and close endpoints exist. |
| 12 | Complete Deposit, Withdrawal, and Transfer Endpoints | Complete | All three money-operation endpoints are implemented. |
| 13 | Standardize Validation and API Error Handling | Complete | Request validation and stable structured errors are implemented. |
| 14 | Add OpenAPI and Swagger Documentation | Complete | The verified contract is served unchanged through runtime Swagger UI. |
| 15 | Add Authentication and Authorization | Complete | OAuth2 resource-server security and scope authorization are implemented. |
| 16 | Add Audit Logging, Monitoring, and Health Checks | Complete | Ledger auditing, correlation IDs, health probes, and Prometheus metrics exist. |
| 17 | Add Integration and End-to-End Testing | In progress | Integration coverage exists; production-like PostgreSQL and secured E2E coverage remains. |
| 18 | Add Architecture Tests and Static Analysis | Planned | ArchUnit rules and automated static-analysis quality gates remain. |
| 19 | Add Docker and Local Deployment Configuration | In progress | Docker and Compose exist; complete secured local deployment and developer configuration remain. |
| 20 | Add GitHub Actions Continuous Integration | Complete | Java 21 verification and container-image build jobs are active ahead of sequence. |
| 21 | Complete Security and Production-Readiness Review | Planned | Scheduled after local deployment is complete. |
| 22 | Final Documentation and Open-Source Preparation | Planned | Scheduled after the production-readiness review. |
| 23 | Release Version 1.0.0 | Planned | Scheduled after all release criteria pass. |

## Recovery sequence

The remaining work through Phase 19 will be completed in this order:

1. Add architecture rules and static-analysis gates for Phase 18.
2. Add production-like PostgreSQL and secured E2E testing for Phase 17.
3. Finish secured Docker-based local deployment for Phase 19.

Each recovery item receives focused tests, documentation, a dedicated branch,
and a passing continuous-integration run.

## Accepted roadmap variances

### Phase 8: JDBC instead of JPA

The application deliberately uses Spring JDBC rather than JPA. This preserves
framework-independent domain objects and keeps financial update, locking, and
transaction behavior explicit. The complete decision and its consequences are
recorded in
[ADR 0007](architecture/0007-postgresql-persistence.md).

### Phase 20 completed early

Continuous integration was introduced before all Phase 1–19 recovery work
finished. It remains active because every recovery change benefits from the
existing verification and container-build gates.

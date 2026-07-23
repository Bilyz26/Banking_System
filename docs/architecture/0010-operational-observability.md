# ADR 0010: Operational observability

## Status

Accepted

## Context

A production service must reveal whether it can receive traffic, provide
diagnostic metrics, and connect logs belonging to one request. Domain objects
must not depend on a monitoring vendor or metrics library.

## Decision

Use Spring Boot Actuator and Micrometer at the infrastructure boundary:

- expose aggregate health plus liveness and readiness probes;
- expose diagnostic and Prometheus metrics;
- rely on the standard `http.server.requests` timer for request duration and
  outcome dimensions;
- add a servlet filter that validates or generates `X-Correlation-ID`, returns
  it to the caller, and places it in the logging MDC for the request lifetime.

Health details are never exposed. The secure profile permits health probes
without authentication and requires `banking.monitor` for all other operational
endpoints.

## Consequences

- orchestrators can make safe restart and traffic-routing decisions;
- Prometheus can scrape JVM, process, database-pool, and HTTP metrics;
- operators can correlate client reports with application logs;
- business and application code remain independent of Micrometer;
- the default local profile exposes operational endpoints without
  authentication and must not be used on an untrusted network;
- distributed traces across multiple services are not yet exported.

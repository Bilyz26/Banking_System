# ADR 0011: Container packaging and continuous integration

## Status

Accepted

## Context

Local builds alone do not guarantee that every change compiles, passes tests,
or can be packaged consistently. Production deployment also requires a
repeatable runtime image with a small privilege surface.

## Decision

Build the application in a multi-stage Dockerfile:

- compile with Eclipse Temurin Java 21 JDK and the checked-in Maven Wrapper;
- copy only the executable JAR into a Java 21 JRE runtime stage;
- run as a dedicated non-root user;
- cap JVM container-memory usage and expose a readiness health check.

Run the container with a read-only filesystem, writable `/tmp`, and
`no-new-privileges` in Docker Compose.

On each pull request and push to `main`, GitHub Actions:

1. checks out the source;
2. installs Eclipse Temurin Java 21 and restores the Maven dependency cache;
3. runs `clean verify`;
4. uploads the executable JAR for seven days;
5. builds the container image only after verification succeeds.

Workflow permissions are read-only.

## Consequences

- CI uses the same Java version and Maven Wrapper as local development;
- test failures prevent container packaging;
- the runtime image excludes Maven, source code, and the JDK compiler;
- the container does not run as root and cannot write outside `/tmp`;
- floating Java 21 image tags receive upstream security patches but make image
  bytes vary over time; a release pipeline may pin digests later;
- this phase validates image construction but does not publish an image to a
  registry or deploy it.

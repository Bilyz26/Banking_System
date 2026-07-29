FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
COPY config/ config/
COPY docs/openapi/ docs/openapi/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/banking-system-*.jar /workspace/application.jar

FROM eclipse-temurin:25-jre-alpine AS runtime

RUN apk upgrade --no-cache \
    && addgroup -S banking \
    && adduser -S -G banking -h /app banking

WORKDIR /app
COPY --from=build --chown=banking:banking /workspace/application.jar app.jar

USER banking
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --quiet --output-document=- \
        http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

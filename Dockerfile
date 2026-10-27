FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache su-exec \
    && addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN mkdir -p /app/.docker/uploads \
    && chown -R appuser:appgroup /app \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

# Starts as root so the entrypoint can fix ownership of the bind-mounted
# uploads directory before dropping privileges to appuser to run the JVM.
ENTRYPOINT ["docker-entrypoint.sh"]

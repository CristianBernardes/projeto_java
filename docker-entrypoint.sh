#!/bin/sh
# Runs as root on container start so it can fix ownership of the uploads
# directory — which, as a host bind mount, may be auto-created by the Docker
# engine (as root) or already exist with a different owner — regardless of
# which service starts first. After fixing permissions, privileges are
# dropped to the unprivileged appuser to actually run the JVM.
set -e

mkdir -p /app/.docker/uploads
chown -R appuser:appgroup /app/.docker/uploads

exec su-exec appuser:appgroup java \
  -XX:+UseG1GC \
  -XX:MaxRAMPercentage=75 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -jar app.jar

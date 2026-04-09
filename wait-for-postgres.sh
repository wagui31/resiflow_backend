#!/bin/sh

echo "⏳ Waiting for Postgres..."

until nc -z postgres 5432; do
  echo "Postgres not ready yet..."
  sleep 2
done

echo "✅ Postgres is up"

exec java -jar app.jar
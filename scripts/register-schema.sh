#!/bin/sh

set -e

echo "Aguardando Schema Registry..."

until curl -sf http://schema-registry:8081/subjects >/dev/null
do
  sleep 2
done

echo "Schema Registry disponível."

echo "Preparando schema..."

SCHEMA=$(jq -Rs . < /schemas/schema.avsc)

echo "{\"schema\":${SCHEMA}}" > /tmp/transaction-schema.json

echo "Registrando schema..."

curl -f -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  http://schema-registry:8081/subjects/transactions.v1-value/versions \
  -d @/tmp/transaction-schema.json

echo
echo "Schema registrado!"
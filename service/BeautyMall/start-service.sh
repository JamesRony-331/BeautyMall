#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ ! -f .env ]; then
  echo '请复制 .env.example 为 .env 并填写本地配置。'
  exit 1
fi
set -a
. ./.env
set +a
: "${DB_PASSWORD:?请配置 DB_PASSWORD}"
: "${JWT_SECRET:?请配置 JWT_SECRET}"
exec ./mvnw spring-boot:run "$@"

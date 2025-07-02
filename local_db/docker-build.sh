#!/bin/bash
NAME=team-14
VERSION="1.0.2"

source .env

sudo docker buildx build \
  --platform linux/amd64 \
  --build-arg POSTGRES_DB=${POSTGRES_DB} \
  --build-arg POSTGRES_USER=${POSTGRES_USER} \
  --build-arg POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
  --no-cache \
  -t amdp-registry.skala-ai.com/skala25a/${NAME}-postgresql:${VERSION} --push ./postgresql

sudo docker buildx build \
  --platform linux/amd64 \
  --no-cache \
  -t amdp-registry.skala-ai.com/skala25a/${NAME}-redis:${VERSION} --push ./redis
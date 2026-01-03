#!/bin/bash

if ! docker info > /dev/null 2>&1; then
  echo "Error: Docker is not running or not installed."
  echo "Please start Docker Desktop and try again."
  exit 1
fi

echo "Starting GDS SWE Challenge App via Docker..."
docker-compose up --build

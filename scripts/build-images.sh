#!/usr/bin/env bash

set -e

echo "========================================"
echo "Building Docker images for Minikube"
echo "========================================"

# Check that Minikube is running
if ! minikube status >/dev/null 2>&1; then
    echo "Minikube is not running."
    echo "Start it first with:"
    echo "    minikube start"
    exit 1
fi

minikube image build -t user-service:latest ./user-service
minikube image build -t booking-service:latest ./booking-service
minikube image build -t audit-service:latest ./audit-service
minikube image build -t api-gateway:latest ./api-gateway
minikube image build -t ai-chat-service:latest ./ai-chat-service
minikube image build -t frontend:latest ./frontend

echo
echo "All images built successfully."

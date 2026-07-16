#!/usr/bin/env bash

set -e

echo "========================================"
echo "Deploying Hotel Booking System with Helm"
echo "========================================"

# Check that Minikube is running
if ! minikube status >/dev/null 2>&1; then
    echo "Minikube is not running."
    echo "Start it first with:"
    echo "    minikube start"
    exit 1
fi

CHART="./helm/hotel-booking"
RELEASE="hotel-booking"

if [[ "$1" == "--ai" ]]; then
    echo "Deploying with AI components..."
    helm upgrade --install "$RELEASE" "$CHART" \
        --set ai.enabled=true
else
    echo "Deploying without AI components..."
    helm upgrade --install "$RELEASE" "$CHART"
fi

echo
echo "Helm release deployed successfully."
echo
echo "Monitor the deployment with:"
echo "    kubectl get pods -w"
echo
echo "When all pods are Running (or Completed for one-time Jobs),"
echo "press Ctrl+C to stop watching."

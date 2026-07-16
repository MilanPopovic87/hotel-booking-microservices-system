#!/usr/bin/env bash

set -e

echo "========================================"
echo "Deploying Hotel Booking System"
echo "Kubernetes Manifests (without AI)"
echo "========================================"

# Check that Minikube is running
if ! minikube status >/dev/null 2>&1; then
    echo " Minikube is not running."
    echo "Start it first with:"
    echo "    minikube start"
    exit 1
fi

echo "Applying Kubernetes manifests..."

kubectl apply -f k8s/shared/
kubectl apply -f k8s/postgres/
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/kafka-ui/
kubectl apply -f k8s/user-service/
kubectl apply -f k8s/booking-service/
kubectl apply -f k8s/audit-service/
kubectl apply -f k8s/api-gateway/
kubectl apply -f k8s/frontend/
kubectl apply -f k8s/ingress/

echo
echo "Kubernetes manifests applied successfully."
echo
echo "Monitor the deployment with:"
echo "    kubectl get pods -w"
echo
echo "When all pods are Running (or Completed for one-time Jobs),"
echo "press Ctrl+C to stop watching."

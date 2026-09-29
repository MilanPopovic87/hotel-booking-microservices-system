#!/usr/bin/env bash

set -e

echo "========================================"
echo "Hotel Booking System Cleanup"
echo "========================================"

delete_secrets() {
    echo
    echo "Removing deployment Secrets..."

    kubectl delete secret postgres-secret --ignore-not-found
    kubectl delete secret app-secret --ignore-not-found
    kubectl delete secret gemini-secret --ignore-not-found
}

# Check that all resources in the current namespace have been removed.
wait_for_cleanup() {
    echo
    echo "Waiting for Kubernetes resources to terminate..."

    while true; do

        # Count remaining resources.
        pods=$(kubectl get pods -o name 2>/dev/null | wc -l)
        deployments=$(kubectl get deployments -o name 2>/dev/null | wc -l)
        statefulsets=$(kubectl get statefulsets -o name 2>/dev/null | wc -l)
        pvcs=$(kubectl get pvc -o name 2>/dev/null | wc -l)

        total=$((pods + deployments + statefulsets + pvcs))

        if [ "$total" -eq 0 ]; then
            break
        fi

        sleep 2
    done

    echo "Cleanup completed."
}

case "$1" in

    k8s)
        echo "Removing Kubernetes manifest deployment..."

        # Delete resources without waiting; wait_for_cleanup() handles the cleanup check.
        kubectl delete -R -f k8s/ \
            --ignore-not-found \
            --wait=false

        delete_secrets
        wait_for_cleanup
        ;;

    helm)
        echo "Removing Helm release..."

        # Ignore the error if the Helm release does not exist.
        helm uninstall hotel-booking || true

        delete_secrets
        wait_for_cleanup
        ;;

    all)
        echo "Removing Helm release..."

        helm uninstall hotel-booking || true

        echo
        echo "Removing Kubernetes manifest deployment..."

        kubectl delete -R -f k8s/ \
            --ignore-not-found \
            --wait=false

        delete_secrets
        wait_for_cleanup
        ;;

    *)
        echo "Usage:"
        echo "  ./scripts/cleanup.sh k8s"
        echo "  ./scripts/cleanup.sh helm"
        echo "  ./scripts/cleanup.sh all"
        exit 1
        ;;

esac

echo
echo "Remaining pods:"
kubectl get pods

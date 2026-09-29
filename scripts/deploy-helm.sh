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

# Check that .env exists
if [ ! -f .env ]; then
    echo ".env file not found."
    echo "Create it from .env.example first."
    exit 1
fi

CHART="./helm/hotel-booking"
RELEASE="hotel-booking"

# Read a value from .env without executing the file.
get_env_value() {
    grep "^${1}=" .env | head -n 1 | cut -d '=' -f2-
}

POSTGRES_USER=$(get_env_value "POSTGRES_USER")
POSTGRES_PASSWORD=$(get_env_value "POSTGRES_PASSWORD")
JWT_SECRET=$(get_env_value "JWT_SECRET")
JWT_EXPIRATION_MINUTES=$(get_env_value "JWT_EXPIRATION_MINUTES")
AI_ENABLED=$(get_env_value "AI_ENABLED")
AI_SERVICE_TOKEN=$(get_env_value "AI_SERVICE_TOKEN")
GEMINI_API_KEY=$(get_env_value "GEMINI_API_KEY")

# Validate common required values
required_vars=(
    POSTGRES_USER
    POSTGRES_PASSWORD
    JWT_SECRET
    JWT_EXPIRATION_MINUTES
    AI_ENABLED
)

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "$var is missing or empty in .env."
        exit 1
    fi
done

# Determine deployment mode
case "$1" in

    --ollama)
        echo "Deploying with Ollama AI..."

        if [ "$AI_ENABLED" != "true" ]; then
            echo "AI_ENABLED must be true for the Ollama deployment."
            echo "Set AI_ENABLED=true in .env and run this script again."
            exit 1
        fi

        if [ -z "$AI_SERVICE_TOKEN" ]; then
            echo "AI_SERVICE_TOKEN is missing or empty in .env."
            exit 1
        fi

        AI_PROVIDER="ollama"
        ;;

    --gemini)
        echo "Deploying with Gemini AI..."

        if [ "$AI_ENABLED" != "true" ]; then
            echo "AI_ENABLED must be true for the Gemini deployment."
            echo "Set AI_ENABLED=true in .env and run this script again."
            exit 1
        fi

        if [ -z "$AI_SERVICE_TOKEN" ]; then
            echo "AI_SERVICE_TOKEN is missing or empty in .env."
            exit 1
        fi

        if [ -z "$GEMINI_API_KEY" ]; then
            echo "GEMINI_API_KEY is missing or empty in .env."
            exit 1
        fi

        AI_PROVIDER="gemini"
        ;;

    "")
        echo "Deploying without AI..."

        if [ "$AI_ENABLED" != "false" ]; then
            echo "AI_ENABLED must be false for the deployment without AI."
            echo "Set AI_ENABLED=false in .env and run this script again."
            exit 1
        fi

        AI_PROVIDER=""
        ;;

    *)
        echo "Unknown option: $1"
        echo
        echo "Usage:"
        echo "    ./scripts/deploy-helm.sh"
        echo "    ./scripts/deploy-helm.sh --ollama"
        echo "    ./scripts/deploy-helm.sh --gemini"
        exit 1
        ;;

esac

# Create common Secrets after deployment mode validation.
echo "Creating/updating PostgreSQL Secret..."

kubectl create secret generic postgres-secret \
    --from-literal=POSTGRES_USER="$POSTGRES_USER" \
    --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
    --dry-run=client \
    -o yaml | kubectl apply -f -

echo "Creating/updating application Secret..."

if [ "$AI_ENABLED" = "true" ]; then

    kubectl create secret generic app-secret \
        --from-literal=JWT_SECRET="$JWT_SECRET" \
        --from-literal=AI_SERVICE_TOKEN="$AI_SERVICE_TOKEN" \
        --dry-run=client \
        -o yaml | kubectl apply -f -

else

    kubectl create secret generic app-secret \
        --from-literal=JWT_SECRET="$JWT_SECRET" \
        --dry-run=client \
        -o yaml | kubectl apply -f -

fi

# Gemini requires an additional Secret.
if [ "$AI_PROVIDER" = "gemini" ]; then

    echo "Creating/updating Gemini Secret..."

    kubectl create secret generic gemini-secret \
        --from-literal=GEMINI_API_KEY="$GEMINI_API_KEY" \
        --dry-run=client \
        -o yaml | kubectl apply -f -

fi

echo "Deploying Helm release..."

# Wait for resources to become ready and roll back if the deployment fails.
if [ "$AI_PROVIDER" = "ollama" ] || [ "$AI_PROVIDER" = "gemini" ]; then

    helm upgrade --install "$RELEASE" "$CHART" \
        --set app.aiEnabled="$AI_ENABLED" \
        --set ai.provider="$AI_PROVIDER" \
        --set app.jwtExpirationMinutes="$JWT_EXPIRATION_MINUTES" \
        --wait \
        --atomic

else

    helm upgrade --install "$RELEASE" "$CHART" \
        --set app.aiEnabled="$AI_ENABLED" \
        --set app.jwtExpirationMinutes="$JWT_EXPIRATION_MINUTES" \
        --wait \
        --atomic

fi

echo
echo "Helm release deployed successfully."
echo
echo "Monitor the deployment with:"
echo "    kubectl get pods -w"
echo
echo "When all pods are Running (or Completed for one-time Jobs),"
echo "press Ctrl+C to stop watching."

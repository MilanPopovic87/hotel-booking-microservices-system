#!/bin/sh

echo "Waiting for Ollama to start..."

until curl -s http://ollama:11434 > /dev/null; do
  sleep 1
done

echo "Ollama is ready. Pulling model..."

curl -X POST http://ollama:11434/api/pull \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$OLLAMA_MODEL\"}"

echo "Model ready."

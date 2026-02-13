#!/bin/bash
# Criar dados de teste rapidamente. Ajuste BASE_URL e as datas conforme seu período atual.
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "1. Configurando fechamento (21 a 20)..."
curl -s -X PUT "$BASE_URL/api/v1/system-config" -H "Content-Type: application/json" \
  -d '{"closureStartDay":21,"closureEndDay":20}' | jq .

echo -e "\n2. Criando entradas de horas..."
for payload in \
  '{"entryDate":"2025-01-22","hours":8,"description":"Desenvolvimento"}' \
  '{"entryDate":"2025-01-23","hours":6.5,"description":"Reuniões"}' \
  '{"entryDate":"2025-02-10","hours":8,"description":"Desenvolvimento"}'; do
  curl -s -X POST "$BASE_URL/api/v1/entries" -H "Content-Type: application/json" -d "$payload" | jq .
done

echo -e "\n3. Criando ajustes..."
curl -s -X POST "$BASE_URL/api/v1/adjustments" -H "Content-Type: application/json" \
  -d '{"adjustmentDate":"2025-01-21","deltaHours":40,"description":"Saldo inicial"}' | jq .
curl -s -X POST "$BASE_URL/api/v1/adjustments" -H "Content-Type: application/json" \
  -d '{"adjustmentDate":"2025-01-25","deltaHours":-2,"description":"Correção"}' | jq .

echo -e "\n4. Listando entradas do período atual..."
curl -s "$BASE_URL/api/v1/entries?periodCurrent=true" | jq .
echo -e "\n5. Listando ajustes do período atual..."
curl -s "$BASE_URL/api/v1/adjustments?periodCurrent=true" | jq .

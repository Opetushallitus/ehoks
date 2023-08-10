#!/bin/bash

test -z "$VIRKAILIJA" && VIRKAILIJA=virkailija.testiopintopolku.fi

TGT_URL=$(cat scripts/cas-tgt-url.txt)
test -z "$TGT_URL" && echo "Use scripts/create-cas-tgt.sh first" && exit 1

ST=$(curl -s "$TGT_URL" -H 'Caller-id: panutest' --data-urlencode \
  "service=https://$VIRKAILIJA/ehoks-virkailija-backend/cas-security-check")
test -z "$ST" && echo "Service ticket was not created" && exit 1

curl -H "ticket: $ST" -H 'caller-id: panutesti' "$@"

# Example: curl-with-service-ticket.sh 
# -X POST -H 'Content-Type: application/json' -d '{}' 
# 'https://virkailija.testiopintopolku.fi/ehoks-virkailija-backend/api/v1/hoks'

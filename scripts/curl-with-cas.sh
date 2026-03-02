#!/bin/bash

test -z "$VIRKAILIJA" && VIRKAILIJA=virkailija.testiopintopolku.fi
test -z "$SERVICE" \
&& SERVICE="https://$VIRKAILIJA/ehoks-virkailija-backend/cas-security-check"

test "X$1" = "X--pass-st-in-query-param-to" \
	&& URL="$2" && SERVICE="$2" && shift && shift

TGT_URL=$(cat scripts/cas-tgt-url.txt)
test -z "$TGT_URL" && echo "Use scripts/create-cas-tgt.sh first" && exit 1

ST=$(curl -s "$TGT_URL" -H 'Caller-id: panutest' \
	--data-urlencode "service=$SERVICE")
echo "Service ticket: $ST"
test -z "$ST" && echo "Service ticket was not created" && exit 1

if test -z "$URL"; then
	curl -H "ticket: $ST" -H 'caller-id: panutesti' "$@"
else
	curl -H 'caller-id: panutesti' "$@" "$URL?ticket=$ST"
fi

# Example: sh scripts/curl-with-cas.sh
# -X POST -H 'Content-Type: application/json' -d '{}' 
# 'https://virkailija.testiopintopolku.fi/ehoks-virkailija-backend/api/v1/hoks'

# Example: sh scripts/curl-with-cas.sh 
# --pass-st-in-query-param-to https://virkailija.testiopintopolku.fi
# /ehoks-virkailija-backend/api/v1/virkailija/session/opintopolku
# -i -c scripts/session-cookie.txt


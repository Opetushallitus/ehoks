#!/bin/bash

test -z "$2" && echo "Usage: $0 <username> <password> [<cas-server>]" && exit 1
CAS_SERVER=virkailija.testiopintopolku.fi
test -n "$3" && CAS_SERVER="$3"

curl -s -D- -o /dev/null "https://$CAS_SERVER/cas/v1/tickets" \
	-d "username=$1&password=$2" \
| sed -ne 's/^[Ll]ocation: \([^ \r]*\).*$/\1/p' \
> scripts/cas-tgt-url.txt

test -s scripts/cas-tgt-url.txt \
&& echo "CAS ticket-granting-ticket was created and saved"


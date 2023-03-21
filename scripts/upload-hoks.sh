curl -i -b scripts/session-cookie.txt -H 'Content-type: application/json' \
 -H 'Caller-id: curl-poksutin' -d @resources/dev/demo-data/hoks_tuva.json \
 http://localhost:3000/ehoks-virkailija-backend/api/v1/virkailija/oppijat/1.2.246.562.24.44651722625/hoksit 

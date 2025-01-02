#!/bin/bash

echo "Sending error message to Slack webhook $ssm_monitor_slack_webhook"
set -eu
curl -X POST --data '{"blocks":[{"type":"header","text":{"type":"plain_text","text":"⚠️ Error in eHOKS -> Lampi export: check logs","emoji":true}}]}' -H 'content-type: application/json' -H 'accept: application/json' "$ssm_monitor_slack_webhook"

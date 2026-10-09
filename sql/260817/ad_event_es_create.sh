#!/usr/bin/env bash
# 用法：ES_URL=http://127.0.0.1:9200 bash sql/260817/ad_event_es_create.sh
# 如需认证：ES_AUTH='-u user:password' ES_URL=http://127.0.0.1:9200 bash sql/260817/ad_event_es_create.sh
# 说明：PUT index template 本身幂等，重复执行会覆盖同名模板，无副作用。
set -euo pipefail
ES_URL=${ES_URL:-http://127.0.0.1:9200}
ES_AUTH=${ES_AUTH:-}
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
JSON_FILE="$SCRIPT_DIR/ad_event_es_index.json"

TEMPLATE_NAME="ad_event_logs"

echo "create index template $TEMPLATE_NAME (patterns: ad_event_logs_*)"
curl -sS ${ES_AUTH} -X PUT "$ES_URL/_index_template/$TEMPLATE_NAME" -H 'Content-Type: application/json' -d @"$JSON_FILE"
echo
echo "done. verify: curl $ES_URL/_index_template/$TEMPLATE_NAME"

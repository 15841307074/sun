#!/usr/bin/env bash
# 用法：ES_URL=http://127.0.0.1:9200 bash sql/260612/activity_sign_es_create.sh
# 如需认证：ES_AUTH='-u user:password' ES_URL=http://127.0.0.1:9200 bash sql/260612/activity_sign_es_create.sh
set -euo pipefail
ES_URL=${ES_URL:-http://127.0.0.1:9200}
ES_AUTH=${ES_AUTH:-}
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
JSON_FILE="$SCRIPT_DIR/activity_sign_es_index.json"

create_index() {
  local index=$1
  local body
  body=$(python3 - "$JSON_FILE" "$index" <<'PY'
import json, sys
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    data=json.load(f)
print(json.dumps(data[sys.argv[2]], ensure_ascii=False))
PY
)
  if curl -s ${ES_AUTH} -o /dev/null -w '%{http_code}' "$ES_URL/$index" | grep -q '^200$'; then
    echo "$index exists, skip"
  else
    echo "create $index"
    curl -sS ${ES_AUTH} -X PUT "$ES_URL/$index" -H 'Content-Type: application/json' -d "$body"
    echo
  fi
}

create_index promotion_sign_record
create_index promotion_sign_reward_record

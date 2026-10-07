#!/bin/bash
set -euo pipefail

BASE_URL="http://127.0.0.1:16610/admin-api"
ACCESS_TOKEN="088795c6ed25410595eda459f7f0566c"
TENANT_ID="1"
VISIT_TENANT_ID=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base-url)
      BASE_URL="$2"
      shift 2
      ;;
    --token)
      ACCESS_TOKEN="$2"
      shift 2
      ;;
    --tenant-id)
      TENANT_ID="$2"
      shift 2
      ;;
    --visit-tenant-id)
      VISIT_TENANT_ID="$2"
      shift 2
      ;;
    -h|--help)
      echo "Usage:"
      echo "  bash sync-job.sh --token <access-token> [--base-url <url>] [--tenant-id <id>] [--visit-tenant-id <id>]"
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -z "$ACCESS_TOKEN" ]]; then
  echo "Error: --token is required" >&2
  echo "Try: bash sync-job.sh --help" >&2
  exit 1
fi

URL="${BASE_URL%/}/infra/job/sync"

echo "POST $URL"

CURL_ARGS=(
  --location
  --request POST
  "$URL"
  --header "Authorization: Bearer $ACCESS_TOKEN"
  --header "Accept: application/json"
  --header "Content-Type: application/json"
  --header "tenant-id: $TENANT_ID"
)

if [[ -n "$VISIT_TENANT_ID" ]]; then
  CURL_ARGS+=(--header "visit-tenant-id: $VISIT_TENANT_ID")
fi

curl "${CURL_ARGS[@]}"

#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

main() {
    require_runtime
    ensure_layout
    sync_deploy_assets

    ensure_env_file "$ROOT_DIR/shared/.env.base" "$ASSET_DIR/env/.env.base.example"
    ensure_env_file "$ROOT_DIR/shared/.env.prod" "$ASSET_DIR/env/.env.prod.example"
    ensure_env_file "$ROOT_DIR/shared/.env.validation" "$ASSET_DIR/env/.env.validation.example"

    if [[ ! -f "$ROOT_DIR/prod/active_state.env" ]]; then
        cat >"$ROOT_DIR/prod/active_state.env" <<EOF
ACTIVE_COLOR=blue
IMAGE_BLUE=
IMAGE_GREEN=
EOF
    fi

    local active_color
    active_color="$(read_state_value ACTIVE_COLOR || true)"
    if [[ -z "$active_color" ]]; then
        active_color="blue"
    fi
    write_prod_gateway_conf "$active_color" "$ROOT_DIR/base/nginx/prod-gateway.conf"

    compose_base up -d redis redis-exporter prod-gateway validation-gateway npmplus prometheus grafana
    switch_prod_gateway "$active_color" || true
    log "base stack 준비 완료: redis/npmplus/gateway/prometheus/grafana"
}

main "$@"

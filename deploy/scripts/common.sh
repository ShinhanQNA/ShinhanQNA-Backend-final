#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="${ROOT_DIR:-/opt/sw-connect}"
ASSET_DIR="${ASSET_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
NETWORK_NAME="sw-connect-network"

log() {
    printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

fail() {
    log "ERROR: $*"
    exit 1
}

require_runtime() {
    command -v docker >/dev/null 2>&1 || fail "docker 명령을 찾을 수 없습니다."
    docker compose version >/dev/null 2>&1 || fail "docker compose plugin이 필요합니다."
}

ensure_layout() {
    mkdir -p \
        "$ROOT_DIR/base/nginx" \
        "$ROOT_DIR/base/prometheus" \
        "$ROOT_DIR/base/grafana/dashboards" \
        "$ROOT_DIR/base/grafana/provisioning/dashboards" \
        "$ROOT_DIR/base/grafana/provisioning/datasources" \
        "$ROOT_DIR/prod" \
        "$ROOT_DIR/validation" \
        "$ROOT_DIR/scripts" \
        "$ROOT_DIR/shared/uploads/prod" \
        "$ROOT_DIR/shared/uploads/validation"
}

ensure_env_file() {
    local env_file="$1"
    local fallback_file="$2"
    if [[ -f "$env_file" ]]; then
        return 0
    fi
    if [[ -f "$fallback_file" ]]; then
        cp -f "$fallback_file" "$env_file"
        log "기본 env 예시를 생성했습니다: $env_file"
        return 0
    fi
    touch "$env_file"
}

sync_deploy_assets() {
    [[ -f "$ASSET_DIR/base/docker-compose.base.yml" ]] || fail "ASSET_DIR가 올바르지 않습니다: $ASSET_DIR"
    [[ -d "$ASSET_DIR/base/grafana" ]] || fail "Grafana 자산 디렉터리를 찾을 수 없습니다: $ASSET_DIR/base/grafana"

    cp -f "$ASSET_DIR/base/docker-compose.base.yml" "$ROOT_DIR/base/docker-compose.yml"
    cp -f "$ASSET_DIR/base/nginx/validation-gateway.conf" "$ROOT_DIR/base/nginx/validation-gateway.conf"
    cp -f "$ASSET_DIR/base/prometheus/prometheus.yml" "$ROOT_DIR/base/prometheus/prometheus.yml"
    cp -Rf "$ASSET_DIR/base/grafana/." "$ROOT_DIR/base/grafana/"
    cp -f "$ASSET_DIR/prod/docker-compose.prod.yml" "$ROOT_DIR/prod/docker-compose.yml"
    cp -f "$ASSET_DIR/validation/docker-compose.validation.yml" "$ROOT_DIR/validation/docker-compose.yml"
    cp -f "$ASSET_DIR/scripts/"*.sh "$ROOT_DIR/scripts/"
    chmod +x "$ROOT_DIR/scripts/"*.sh
}

write_prod_gateway_conf() {
    local color="$1"
    local output_file="$2"
    cat >"$output_file" <<EOF
server {
    listen 8080;
    server_name _;

    location = /actuator/prometheus {
        return 403;
    }

    location / {
        proxy_pass http://sw-connect-prod-${color}:8080;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host \$host;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_read_timeout 30s;
        proxy_connect_timeout 5s;
    }
}
EOF
}

switch_prod_gateway() {
    local color="$1"
    write_prod_gateway_conf "$color" "$ROOT_DIR/base/nginx/prod-gateway.conf"
    docker exec sw-connect-prod-gateway nginx -t >/dev/null
    docker exec sw-connect-prod-gateway nginx -s reload >/dev/null
}

compose_base() {
    docker compose -f "$ROOT_DIR/base/docker-compose.yml" --env-file "$ROOT_DIR/shared/.env.base" "$@"
}

compose_prod() {
    docker compose --env-file "$ROOT_DIR/prod/.runtime.env" -f "$ROOT_DIR/prod/docker-compose.yml" "$@"
}

compose_validation() {
    docker compose --env-file "$ROOT_DIR/validation/.runtime.env" -f "$ROOT_DIR/validation/docker-compose.yml" "$@"
}

wait_internal_http() {
    local url="$1"
    local timeout_seconds="${2:-120}"
    local started_at
    started_at="$(date +%s)"

    while true; do
        if docker run --rm --network "$NETWORK_NAME" curlimages/curl:8.10.1 -fsS "$url" >/dev/null 2>&1; then
            return 0
        fi

        local now
        now="$(date +%s)"
        if ((now - started_at >= timeout_seconds)); then
            return 1
        fi
        sleep 3
    done
}

dump_container_diagnostics() {
    local container_name="$1"
    local tail_lines="${2:-200}"

    log "----- diagnostics: ${container_name} -----"

    if ! docker ps -a --format '{{.Names}}' | grep -Fxq "$container_name"; then
        log "container not found: ${container_name}"
        return 0
    fi

    docker ps -a --filter "name=^/${container_name}$" \
        --format 'name={{.Names}} status={{.Status}} image={{.Image}}' || true
    docker inspect --format \
        'state={{.State.Status}} running={{.State.Running}} restarting={{.State.Restarting}} exitCode={{.State.ExitCode}} error={{.State.Error}} startedAt={{.State.StartedAt}} finishedAt={{.State.FinishedAt}}' \
        "$container_name" || true
    docker logs --tail "$tail_lines" "$container_name" 2>&1 || true

    log "----- end diagnostics: ${container_name} -----"
}

dump_failure_diagnostics() {
    local reason="$1"
    shift

    log "실패 진단 시작: ${reason}"
    for container_name in "$@"; do
        dump_container_diagnostics "$container_name"
    done
    log "실패 진단 종료: ${reason}"
}

read_state_value() {
    local key="$1"
    local state_file="$ROOT_DIR/prod/active_state.env"
    if [[ ! -f "$state_file" ]]; then
        return 1
    fi
    awk -F= -v k="$key" '$1 == k { print $2 }' "$state_file"
}

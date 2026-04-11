#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

IMAGE_REF="${IMAGE_REF:-${1:-}}"
if [[ -z "$IMAGE_REF" ]]; then
    fail "IMAGE_REF를 지정해야 합니다. 예: IMAGE_REF=ghcr.io/org/app:sha-abc123"
fi

main() {
    "$SCRIPT_DIR/bootstrap_base.sh"

    local active_color image_blue image_green target_color previous_color host_uid host_gid
    active_color="$(read_state_value ACTIVE_COLOR || true)"
    image_blue="$(read_state_value IMAGE_BLUE || true)"
    image_green="$(read_state_value IMAGE_GREEN || true)"
    host_uid="$(id -u)"
    host_gid="$(id -g)"

    if [[ -z "$active_color" ]]; then
        active_color="blue"
    fi
    if [[ -z "$image_blue" ]]; then
        image_blue="$IMAGE_REF"
    fi
    if [[ -z "$image_green" ]]; then
        image_green="$IMAGE_REF"
    fi

    if [[ "$active_color" == "blue" ]]; then
        target_color="green"
    else
        target_color="blue"
    fi
    previous_color="$active_color"

    if [[ "$target_color" == "blue" ]]; then
        image_blue="$IMAGE_REF"
    else
        image_green="$IMAGE_REF"
    fi

    cat >"$ROOT_DIR/prod/.runtime.env" <<EOF
IMAGE_BLUE=${image_blue}
IMAGE_GREEN=${image_green}
HOST_UID=${host_uid}
HOST_GID=${host_gid}
EOF

    log "prod 배포 시작: target=${target_color}, image=${IMAGE_REF}"
    compose_prod up -d "app_${target_color}"

    if ! wait_internal_http "http://sw-connect-prod-${target_color}:8080/actuator/health" 240; then
        dump_failure_diagnostics \
            "신규 ${target_color} 앱 health check 실패" \
            "sw-connect-prod-${target_color}" \
            sw-connect-prod-gateway \
            sw-connect-validation-gateway
        compose_prod stop "app_${target_color}" || true
        fail "신규 ${target_color} 앱 health check 실패"
    fi

    if ! switch_prod_gateway "$target_color"; then
        dump_failure_diagnostics \
            "gateway 전환 실패" \
            sw-connect-prod-gateway \
            "sw-connect-prod-${target_color}"
        compose_prod stop "app_${target_color}" || true
        switch_prod_gateway "$previous_color" || true
        fail "gateway 전환 실패"
    fi

    if ! wait_internal_http "http://sw-connect-prod-gateway:8080/actuator/health" 180; then
        dump_failure_diagnostics \
            "gateway 전환 후 health check 실패" \
            sw-connect-prod-gateway \
            "sw-connect-prod-${target_color}" \
            "sw-connect-prod-${previous_color}"
        switch_prod_gateway "$previous_color" || true
        compose_prod stop "app_${target_color}" || true
        fail "gateway 전환 후 health check 실패"
    fi

    if [[ "$previous_color" != "$target_color" ]]; then
        compose_prod stop "app_${previous_color}" || true
    fi

    cat >"$ROOT_DIR/prod/active_state.env" <<EOF
ACTIVE_COLOR=${target_color}
IMAGE_BLUE=${image_blue}
IMAGE_GREEN=${image_green}
EOF

    log "prod 무중단 배포 성공: active=${target_color}"
}

main "$@"

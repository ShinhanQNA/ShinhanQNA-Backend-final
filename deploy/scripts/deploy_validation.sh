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

    local host_uid host_gid
    host_uid="$(id -u)"
    host_gid="$(id -g)"

    cat >"$ROOT_DIR/validation/.runtime.env" <<EOF
IMAGE_VALIDATION=${IMAGE_REF}
HOST_UID=${host_uid}
HOST_GID=${host_gid}
EOF

    log "validation 배포 시작: ${IMAGE_REF}"
    compose_validation up -d --force-recreate app_validation

    if ! wait_internal_http "http://sw-connect-validation:8080/actuator/health" 180; then
        dump_failure_diagnostics \
            "validation 앱 health check 실패" \
            sw-connect-validation \
            sw-connect-validation-gateway \
            sw-connect-prod-gateway
        fail "validation 앱 health check 실패"
    fi

    if ! wait_internal_http "http://sw-connect-validation-gateway:8080/actuator/health" 180; then
        dump_failure_diagnostics \
            "validation gateway health check 실패" \
            sw-connect-validation-gateway \
            sw-connect-validation \
            sw-connect-prod-gateway
        fail "validation gateway health check 실패"
    fi

    log "validation 배포 성공"
}

main "$@"

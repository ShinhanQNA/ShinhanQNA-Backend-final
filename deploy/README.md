# SW Connect Deploy Guide

## 개요
- 단일 VM 경로는 `/opt/sw-connect`를 사용한다.
- 공통 스택은 `base`(redis, node-exporter, npmplus, gateway, prometheus, grafana), 앱 스택은 `prod`, `validation`으로 분리한다.
- `main`은 prod blue/green 무중단 배포, `develop`은 validation 재기동 배포를 사용한다.
- `/actuator/prometheus`는 gateway에서 `403`으로 차단하여 외부 노출하지 않는다(내부 컨테이너 네트워크에서만 스크랩).

## GitHub Secrets
다음 시크릿을 저장소에 등록한다.

- `SW_VM_HOST`
- `SW_VM_PORT`
- `SW_VM_USER`
- `SW_VM_SSH_KEY`
- `GHCR_USERNAME` (GHCR 패키지 read 가능한 계정명, 예: `bindoong01`)
- `GHCR_READ_TOKEN` (GHCR `read:packages` 권한 PAT)
- `SW_DEPLOY_ENV_BASE` (멀티라인 가능, `/opt/sw-connect/shared/.env.base`로 저장)
- `SW_DEPLOY_ENV_PROD` (멀티라인 가능, `/opt/sw-connect/shared/.env.prod`로 저장)
- `SW_DEPLOY_ENV_VALIDATION` (멀티라인 가능, `/opt/sw-connect/shared/.env.validation`로 저장)

주의:
- `GRAFANA_ADMIN_PASSWORD`는 필수 값이다(미설정 시 base stack 기동 실패).
- `GHCR_READ_TOKEN`은 최소 권한 `read:packages`만 부여한다.

## npmplus 초기 1회 설정
CD는 npmplus 컨테이너를 자동 기동하지만, 프록시 호스트 등록은 초기 1회 수동 설정이 필요하다.

1. SSH 터널로 npmplus UI 접근
2. `api.hdb01.site` -> `sw-connect-prod-gateway:8080` 프록시 등록
3. `valid-api.hdb01.site` -> `sw-connect-validation-gateway:8080` 프록시 등록
4. 각 도메인에서 SSL 발급/자동갱신 + HTTP/3 활성화

## 원격 수동 실행 예시
```bash
ROOT_DIR=/opt/sw-connect \
ASSET_DIR=/opt/sw-connect/shared/artifacts/deploy \
IMAGE_REF=ghcr.io/shinhanqna/sw-connect-backend:sha-abcdef1 \
bash /opt/sw-connect/shared/artifacts/deploy/scripts/deploy_validation.sh
```

```bash
ROOT_DIR=/opt/sw-connect \
ASSET_DIR=/opt/sw-connect/shared/artifacts/deploy \
IMAGE_REF=ghcr.io/shinhanqna/sw-connect-backend:sha-abcdef1 \
bash /opt/sw-connect/shared/artifacts/deploy/scripts/deploy_prod.sh
```

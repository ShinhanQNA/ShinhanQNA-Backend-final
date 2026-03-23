#!/usr/bin/env bash
set -euo pipefail

latest_tag="$(git tag --list '[0-9]*.[0-9]*.[0-9]*' --sort=-v:refname | head -n 1)"

if [[ -z "$latest_tag" ]]; then
    echo "1.0.1"
    exit 0
fi

IFS='.' read -r major minor patch <<<"$latest_tag"
if [[ -z "${major:-}" || -z "${minor:-}" || -z "${patch:-}" ]]; then
    echo "1.0.1"
    exit 0
fi

echo "${major}.${minor}.$((patch + 1))"

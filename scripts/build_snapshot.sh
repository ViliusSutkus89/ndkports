#!/bin/bash
set -e
docker build -t ndkports .
TARGETS="${@:-release}"
docker run --rm -u $(id -u ${USER}):$(id -g ${USER}) -v $(pwd):/src ndkports "${TARGETS}"

#!/bin/bash
set -e
docker build -t ndkports .
docker run --rm -u $(id -u ${USER}):$(id -g ${USER}) -v $(pwd):/src ndkports

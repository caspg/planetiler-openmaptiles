#!/usr/bin/env bash
set -e

JAVA_TOOL_OPTIONS="-Xmx4g" java -jar target/*with-deps.jar \
  --force \
  --download \
  --area=pomorskie  \
  --languages=en,pl,latin \
  --max_point_buffer=4 \
  --exclude_layers=housenumber \
  --download-threads=5 --download-chunk-size-mb=1000 \
  --threads=5 \
  —gzip-temp \
  --nodemap-type=array --storage=mmap 2>&1 | tee logs.txt

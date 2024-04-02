#!/usr/bin/env bash
set -e

JAVA_TOOL_OPTIONS="-Xmx32g" java -jar target/*with-deps.jar \
  --force \
  --download \
  --area=europe  \
  --languages=en,pl,latin \
  --bounds=planet \
  --max_point_buffer=4 \
  --exclude_layers=housenumber \
  --download-threads=5 --download-chunk-size-mb=1000 \
  --threads=5
  --nodemap-type=array --storage=mmap 2>&1 | tee logs.txt

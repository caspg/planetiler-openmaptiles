#!/usr/bin/env bash
set -e

JAVA_TOOL_OPTIONS="-Xmx32g" java -jar target/*with-deps.jar \
  --force \
  --download \
  --area=europe  \
  --bounds=planet \
  --max_point_buffer=4 \
  --exclude_layers=housenumber \
  --download-threads=10 --download-chunk-size-mb=1000 \
  --nodemap-type=array --storage=mmap 2>&1 | tee logs.txt

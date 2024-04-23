#!/usr/bin/env bash
set -e

# - MaxHeapFreeRatio - return unused heap memory to the OS

# - --storage=ram - use RAM storage for temporary data
# which requires 1.5x more memory than the input data size
# Europe OSM data is about 30GB so we need at least 45GB of RAM

JAVA_TOOL_OPTIONS="-Xmx48g" java -jar target/*with-deps.jar \
  -XX:MaxHeapFreeRatio=40 \
  --force \
  --download \
  --area=europe  \
  --languages=en,pl,latin \
  --bounds=planet \
  --max_point_buffer=4 \
  --exclude_layers=housenumber \
  --download-threads=8 \
  --download-chunk-size-mb=1000 \
  --threads=8 \
  --nodemap-type=array --storage=ram 2>&1 | tee logs.txt

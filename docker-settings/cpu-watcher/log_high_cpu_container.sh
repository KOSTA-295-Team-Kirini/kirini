#!/bin/bash
LOG_FILE="/logs/high_cpu_log.txt"
mkdir -p "$(dirname "$LOG_FILE")"

TOP_CONTAINER=$(docker stats --no-stream --format "{{.Container}} {{.Name}} {{.CPUPerc}}" \
  | sort -k3 -r \
  | head -n1)

CPU_USAGE=$(echo "$TOP_CONTAINER" | awk '{gsub(/%/, "", $3); print $3}')
if (( $(echo "$CPU_USAGE > 80.0" | bc -l) )); then
    TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$TIMESTAMP] HIGH CPU USAGE DETECTED: $TOP_CONTAINER" >> "$LOG_FILE"
fi

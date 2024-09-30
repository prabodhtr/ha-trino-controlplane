#!/bin/sh
set -e

echo "Generating envoy_final.yaml config file..."
cat /etc/envoy/envoy.yaml | envsubst \$CONTROLPLANE_XDS_PORT,\$CONTROLPLANE_CACHE_PORT,\$ENVOY_ADMIN_PORT > /etc/envoy/envoy_final.yaml

echo "Starting Envoy..."
/usr/local/bin/envoy -c /etc/envoy/envoy_final.yaml
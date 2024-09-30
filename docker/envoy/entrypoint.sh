#!/bin/sh
set -e

echo "Generating envoy_config.yaml file..."
cat /etc/envoy/envoy.yaml | envsubst \$CONTROLPLANE_XDS_PORT,\$CONTROLPLANE_CACHE_PORT,\$ENVOY_ADMIN_PORT > /envoy_config.yaml

echo "Starting Envoy..."
/usr/local/bin/envoy -c /envoy_config.yaml
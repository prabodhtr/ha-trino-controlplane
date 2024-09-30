# Retrieve the script directory.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "${SCRIPT_DIR}" || exit 2

# SOURCE_DIR is set to project root
SOURCE_DIR="${SCRIPT_DIR}/../../"

VERSION=latest

echo "Preparing the image build context directory"
WORK_DIR="$(mktemp -d)"

TAG="custom_envoyproxy:${VERSION}"
cp "$SOURCE_DIR/docker/envoy/entrypoint.sh" "${WORK_DIR}/"

echo "Building the image for envoyproxy with tag ${TAG}"
docker build \
    "${WORK_DIR}" \
    --pull \
    -f Dockerfile \
    -t "${TAG}"

echo "Cleaning up the build context directory"
rm -r "${WORK_DIR}"

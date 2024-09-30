# Retrieve the script directory.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "${SCRIPT_DIR}" || exit 2

SOURCE_DIR="${SCRIPT_DIR}/../../../"
VERSION=latest

echo "Preparing the image build context directory"
WORK_DIR="$(mktemp -d)"
cp "$SOURCE_DIR/target/trino-control-plane.jar" "${WORK_DIR}/"
cp "$SOURCE_DIR/src/main/resources/application.yaml" "${WORK_DIR}/"

TAG="trino-controlplane:${VERSION}"

echo "Building the image for trino-control plane with tag ${TAG}"
docker build \
    "${WORK_DIR}" \
    --pull \
    -f Dockerfile \
    -t "${TAG}"

echo "Cleaning up the build context directory"
rm -r "${WORK_DIR}"

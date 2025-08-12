#!/usr/bin/env bash
# Deploy RealConnect Agent: build Docker image, push to ECR, update ECS service task definition.
# Requires: aws cli v2, docker, jq, git
set -euo pipefail

### ---- DEFAULTS (override via env) ----
: "${ACCOUNT_ID:=805598450324}"
: "${ECR_REPO:=edifymcp}"
: "${IMAGE_REGION:=us-east-1}"        # ECR registry region (your ECR is here)
: "${ECS_REGION:=${IMAGE_REGION}}"    # ECS cluster region (override if different, e.g., us-west-2)
: "${CLUSTER_NAME:=realconnect-cluster}"
: "${SERVICE_NAME:=realconnect-agent-svc}"
: "${CONTAINER_NAME:=agent-api}"      # name of the container in the task definition to update

# Build context & Dockerfile (adjust if your service lives elsewhere)
: "${BUILD_CONTEXT:=services/agent-api}"
: "${DOCKERFILE:=services/agent-api/Dockerfile}"

# Optional: provide your own image tag
: "${IMAGE_TAG:=}"
if [[ -z "${IMAGE_TAG}" ]]; then
  IMAGE_TAG="$(git rev-parse --short HEAD 2>/dev/null || date +%s)"
fi

REGISTRY="${ACCOUNT_ID}.dkr.ecr.${IMAGE_REGION}.amazonaws.com"
IMAGE_URI="${REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
IMAGE_URI_LATEST="${REGISTRY}/${ECR_REPO}:latest"

usage() {
  cat <<EOF
Usage: env [OVERRIDES] ./deploy_agent.sh

Env overrides:
  ACCOUNT_ID       (default: 805598450324)
  ECR_REPO         (default: edifymcp)
  IMAGE_REGION     (default: us-east-1)       # ECR region
  ECS_REGION       (default: IMAGE_REGION)    # ECS cluster region
  CLUSTER_NAME     (default: realconnect-cluster)
  SERVICE_NAME     (default: realconnect-agent-svc)
  CONTAINER_NAME   (default: agent-api)
  BUILD_CONTEXT    (default: services/agent-api)
  DOCKERFILE       (default: services/agent-api/Dockerfile)
  IMAGE_TAG        (default: git short SHA or timestamp)

Examples:
  # ECR & ECS both us-east-1
  ./deploy_agent.sh

  # ECR in us-east-1, ECS in us-west-2 (closer to HI)
  ECS_REGION=us-west-2 ./deploy_agent.sh
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage; exit 0
fi

command -v aws >/dev/null || { echo "aws CLI not found"; exit 1; }
command -v docker >/dev/null || { echo "docker not found"; exit 1; }
command -v jq >/dev/null || { echo "jq not found (sudo apt-get install -y jq)"; exit 1; }

echo "==> ECR: ${REGISTRY}/${ECR_REPO} (region: ${IMAGE_REGION})"
echo "==> ECS: cluster=${CLUSTER_NAME}, service=${SERVICE_NAME} (region: ${ECS_REGION})"
echo "==> Container to update in task def: ${CONTAINER_NAME}"
echo "==> Build context: ${BUILD_CONTEXT}, Dockerfile: ${DOCKERFILE}"
echo "==> Image tags: ${IMAGE_URI} and ${IMAGE_URI_LATEST}"

### 1) Ensure ECR repository exists
if ! aws ecr describe-repositories --region "${IMAGE_REGION}" --repository-names "${ECR_REPO}" >/dev/null 2>&1; then
  echo "==> Creating ECR repository ${ECR_REPO} in ${IMAGE_REGION} ..."
  aws ecr create-repository \
    --repository-name "${ECR_REPO}" \
    --image-scanning-configuration scanOnPush=true \
    --encryption-configuration encryptionType=AES256 \
    --region "${IMAGE_REGION}" >/dev/null
else
  echo "==> ECR repository ${ECR_REPO} already exists."
fi

### 2) Login Docker to ECR
aws ecr get-login-password --region "${IMAGE_REGION}" | docker login --username AWS --password-stdin "${REGISTRY}"

### 3) Build and push image
echo "==> Building image ..."
docker build -f "${DOCKERFILE}" -t "${ECR_REPO}:${IMAGE_TAG}" "${BUILD_CONTEXT}"
docker tag "${ECR_REPO}:${IMAGE_TAG}" "${IMAGE_URI}"
docker tag "${ECR_REPO}:${IMAGE_TAG}" "${IMAGE_URI_LATEST}"

echo "==> Pushing image tags ..."
docker push "${IMAGE_URI}"
docker push "${IMAGE_URI_LATEST}"

### 4) Update ECS service to new image
echo "==> Describing ECS service to get current task definition ..."
SERVICE_DESC=$(aws ecs describe-services --cluster "${CLUSTER_NAME}" --services "${SERVICE_NAME}" --region "${ECS_REGION}" || true)
STATUS=$(echo "${SERVICE_DESC}" | jq -r '.services[0].status // empty')

if [[ "${STATUS}" != "ACTIVE" ]]; then
  echo "!! ECS service ${SERVICE_NAME} not found or not ACTIVE in ${ECS_REGION}. Skipping ECS update."
  echo "   Use this image in your task definition: ${IMAGE_URI_LATEST}"
  exit 0
fi

TASK_DEF_ARN=$(echo "${SERVICE_DESC}" | jq -r '.services[0].taskDefinition')
echo "==> Current task definition: ${TASK_DEF_ARN}"

echo "==> Describing current task definition JSON ..."
TD_JSON=$(aws ecs describe-task-definition --task-definition "${TASK_DEF_ARN}" --region "${ECS_REGION}")

FAMILY=$(echo "${TD_JSON}" | jq -r '.taskDefinition.family')

# Update the image for the specified container (or the first if not found)
NEW_CONTAINER_DEFS=$(echo "${TD_JSON}" | jq --arg IMG "${IMAGE_URI_LATEST}" --arg NAME "${CONTAINER_NAME}" '
  .taskDefinition.containerDefinitions
  | (if any(.name == $NAME; .) then
       map(if .name == $NAME then .image = $IMG else . end)
     else
       (.[0].image = $IMG) as $x | .
     end)
')

REGISTER_JSON=$(echo "${TD_JSON}" | jq --argjson newDefs "${NEW_CONTAINER_DEFS}" '{
  family: .taskDefinition.family,
  taskRoleArn: .taskDefinition.taskRoleArn,
  executionRoleArn: .taskDefinition.executionRoleArn,
  networkMode: .taskDefinition.networkMode,
  containerDefinitions: $newDefs,
  volumes: .taskDefinition.volumes,
  placementConstraints: .taskDefinition.placementConstraints,
  requiresCompatibilities: .taskDefinition.requiresCompatibilities,
  cpu: .taskDefinition.cpu,
  memory: .taskDefinition.memory,
  pidMode: .taskDefinition.pidMode,
  ipcMode: .taskDefinition.ipcMode,
  proxyConfiguration: .taskDefinition.proxyConfiguration,
  inferenceAccelerators: .taskDefinition.inferenceAccelerators,
  ephemeralStorage: .taskDefinition.ephemeralStorage,
  runtimePlatform: .taskDefinition.runtimePlatform
}')

echo "==> Registering new task definition revision for family ${FAMILY} ..."
NEW_TD_ARN=$(aws ecs register-task-definition \
  --region "${ECS_REGION}" \
  --cli-input-json "${REGISTER_JSON}" \
  | jq -r '.taskDefinition.taskDefinitionArn')

echo "==> Updating ECS service to ${NEW_TD_ARN} ..."
aws ecs update-service \
  --cluster "${CLUSTER_NAME}" \
  --service "${SERVICE_NAME}" \
  --task-definition "${NEW_TD_ARN}" \
  --region "${ECS_REGION}" >/dev/null

echo "==> Forcing new deployment ..."
aws ecs update-service \
  --cluster "${CLUSTER_NAME}" \
  --service "${SERVICE_NAME}" \
  --force-new-deployment \
  --region "${ECS_REGION}" >/dev/null

echo "==> Waiting for service stability ..."
aws ecs wait services-stable \
  --cluster "${CLUSTER_NAME}" \
  --services "${SERVICE_NAME}" \
  --region "${ECS_REGION}"

echo "✅ Done. ECS service updated to image: ${IMAGE_URI_LATEST}"
echo "   Tip: Health check path should be /healthz on port 8080 in the ALB target group."
#!/bin/bash
IMAGE_NAME="team-14-backend"
VERSION="1.0.1"
#IS_CACHE="--no-cache"

echo "Building Gradle project..."
(cd yaxim && ./gradlew clean build)

# 빌드 성공 확인
if [ $? -eq 0 ]; then
    echo "Gradle build successful. Building Docker image..."
    
    # Docker 이미지 빌드
    sudo docker buildx build \
      --platform linux/amd64 \
      --tag amdp-registry.skala-ai.com/skala25a/${IMAGE_NAME}:1.0.1 \
      --push .
else
    echo "Gradle build failed. Aborting Docker build."
    exit 1
fi

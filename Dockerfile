# HMH Ktor Server Dockerfile
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Gradle Wrapper와 소스 코드 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle .
COPY gradle.properties .
COPY src src

# 빌드 권한 설정
RUN chmod +x ./gradlew

# 애플리케이션 빌드
RUN ./gradlew build -x test

# 포트 노출
EXPOSE 8080

# 환경 변수 설정
ENV KTOR_ENV=prod
ENV KTOR_PORT=8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 애플리케이션 실행
CMD ["./gradlew", "run"]
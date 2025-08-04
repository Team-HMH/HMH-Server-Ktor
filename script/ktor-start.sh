#!/bin/bash
# HMH Ktor Server 배포 스크립트

# 설정
APPLICATION_PATH=/home/ubuntu/app
APP_NAME="HMH-Server-Ktor"
ENVIRONMENT=${DEPLOYMENT_GROUP_NAME:-"dev"}

cd $APPLICATION_PATH

# 기존 프로세스 종료
PID=$(pgrep -f "kotlin.*Application")
if [ -n "$PID" ]; then
    echo "> 기존 애플리케이션 종료 중... PID: $PID"
    sudo kill -15 $PID
    sleep 10
    
    # SIGTERM으로 종료되지 않으면 강제 종료
    if ps -p $PID > /dev/null; then
        echo "> 강제 종료 중..."
        sudo kill -9 $PID
    fi
else
    echo "> 현재 구동중인 애플리케이션이 없습니다."
fi

# 환경 변수 로드
source ~/.bashrc

# 배포 환경별 설정
case "$DEPLOYMENT_GROUP_NAME" in
    "hmh-dev-deploy-group")
        echo "> 개발 환경으로 배포 중..."
        PROFILE="dev"
        ;;
    "hmh-prod-deploy-group") 
        echo "> 운영 환경으로 배포 중..."
        PROFILE="prod"
        ;;
    *)
        echo "> 기본 환경으로 배포 중..."
        PROFILE="dev"
        ;;
esac

# Ktor 애플리케이션 실행
echo "> Ktor 애플리케이션 시작: Profile=$PROFILE"

# 환경 변수 설정
export KTOR_ENV=$PROFILE
export KTOR_PORT=8080

# nohup으로 백그라운드 실행
nohup ./gradlew run > logs/application.log 2>&1 &

# 프로세스 확인
sleep 5
NEW_PID=$(pgrep -f "kotlin.*Application")
if [ -n "$NEW_PID" ]; then
    echo "> 애플리케이션 시작 완료! PID: $NEW_PID"
    echo "> 로그 확인: tail -f logs/application.log"
else
    echo "> 애플리케이션 시작 실패!"
    exit 1
fi
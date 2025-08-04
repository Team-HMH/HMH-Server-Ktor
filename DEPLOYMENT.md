# HMH Ktor Server 배포 가이드

## 🚀 배포 방법 선택

### 방법 1: Docker를 사용한 배포 (추천)

#### 1.1 사전 준비
```bash
# Docker 및 Docker Compose 설치 확인
docker --version
docker-compose --version
```

#### 1.2 환경 변수 설정
```bash
# 환경 변수 파일 복사 및 수정
cp .env.example .env
vi .env  # 실제 값으로 수정
```

#### 1.3 Docker로 실행
```bash
# 애플리케이션 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f hmh-server

# 상태 확인
docker-compose ps
```

#### 1.4 접속 확인
```bash
# 헬스체크
curl http://localhost:8080/health

# API 테스트
curl http://localhost:8080/api/v1/point/earn
```

### 방법 2: AWS EC2 인스턴스 배포

#### 2.1 EC2 인스턴스 준비
```bash
# Ubuntu 20.04 LTS 추천
# 인스턴스 타입: t3.micro 이상
# 보안 그룹: 8080 포트 개방
```

#### 2.2 서버 환경 설정
```bash
# Java 17 설치
sudo apt update
sudo apt install openjdk-17-jdk -y

# MySQL 설치 (또는 RDS 사용)
sudo apt install mysql-server -y

# Git 설치
sudo apt install git -y
```

#### 2.3 애플리케이션 배포
```bash
# 프로젝트 클론
git clone <your-repository-url> /home/ubuntu/app
cd /home/ubuntu/app

# 환경 변수 설정
cp .env.example .env
sudo vi .env  # 실제 값으로 수정

# 배포 스크립트 실행
./script/ktor-start.sh
```

### 방법 3: Heroku 배포 (간단한 테스트용)

#### 3.1 Heroku CLI 설치 및 로그인
```bash
# Heroku CLI 설치
# https://devcenter.heroku.com/articles/heroku-cli

heroku login
```

#### 3.2 Heroku 앱 생성
```bash
heroku create your-app-name
```

#### 3.3 환경 변수 설정
```bash
heroku config:set JWT_SECRET=your-secret-key
heroku config:set DB_HOST=your-db-host
heroku config:set DB_USER=your-db-user
heroku config:set DB_PASSWORD=your-db-password
# ... 기타 환경 변수들
```

#### 3.4 배포
```bash
git add .
git commit -m "Deploy to Heroku"
git push heroku main
```

## 🔧 운영 환경 설정

### 환경 변수 필수 설정

1. **데이터베이스 설정**
   - `DB_HOST`: 데이터베이스 호스트
   - `DB_USER`: 데이터베이스 사용자
   - `DB_PASSWORD`: 데이터베이스 비밀번호

2. **보안 설정**
   - `JWT_SECRET`: 32자 이상의 안전한 비밀키
   - `ALLOWED_HOSTS`: 허용할 도메인 목록

3. **OAuth 설정 (선택사항)**
   - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
   - `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_CLIENT_ID`

### 로그 모니터링

```bash
# Docker 환경
docker-compose logs -f hmh-server

# 일반 환경
tail -f logs/application.log
```

### 헬스체크 엔드포인트

- `GET /health` - 기본 상태 확인
- `GET /health/ready` - 준비 상태 확인
- `GET /health/live` - 생존 상태 확인

## 🐛 트러블슈팅

### 일반적인 문제들

1. **포트 충돌**
   ```bash
   # 8080 포트 사용 중인 프로세스 확인
   lsof -i :8080
   
   # 프로세스 종료
   sudo kill -9 <PID>
   ```

2. **데이터베이스 연결 실패**
   - DB 호스트, 포트, 사용자 정보 확인
   - 방화벽 설정 확인
   - MySQL 서비스 상태 확인

3. **JWT 오류**
   - JWT_SECRET 환경 변수 설정 확인
   - 32자 이상의 안전한 키 사용

4. **메모리 부족**
   ```bash
   # Java 힙 메모리 제한 설정
   export JAVA_OPTS="-Xmx512m -Xms256m"
   ```

## 📊 모니터링

### 권장 모니터링 도구
- **로그**: ELK Stack 또는 CloudWatch
- **메트릭**: Prometheus + Grafana
- **알림**: Slack 웹훅 활용

### 성능 최적화
- DB 커넥션 풀 크기 조정
- JVM 힙 메모리 튜닝
- Nginx 리버스 프록시 사용
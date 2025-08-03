# HMH 프로젝트 - Spring Boot to Ktor 마이그레이션

## 프로젝트 개요
- **프로젝트명**: HMH (Habit Making Helper)
- **현재 상태**: Spring Boot 3.2.1 + Java 17 기반
- **목표**: Ktor + Kotlin으로 마이그레이션
- **그룹ID**: sopt.org
- **버전**: 0.0.1-SNAPSHOT

## 현재 기술 스택 (Spring Boot)
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.1
- **빌드 도구**: Gradle
- **데이터베이스**: MySQL
- **ORM**: Spring Data JPA + QueryDSL
- **보안**: Spring Security + JWT
- **API 문서**: SpringDoc OpenAPI (Swagger)
- **외부 연동**: OpenFeign (카카오, 애플 로그인)
- **모니터링**: Slack API

## 도메인 구조
프로젝트는 다음과 같은 도메인으로 구성되어 있습니다:

### 핵심 도메인
- **auth**: 인증/인가 (소셜 로그인, JWT)
- **user**: 사용자 관리
- **challenge**: 챌린지 관리
- **dailychallenge**: 일일 챌린지
- **app**: 앱 관리 및 히스토리
- **point**: 포인트 시스템
- **admin**: 관리자 기능
- **banner**: 배너 관리

### 인프라/공통
- **global/auth**: JWT, 소셜 로그인 (카카오, 애플)
- **global/common**: 공통 예외, 응답 포맷
- **global/config**: 설정 클래스들
- **slack**: 슬랙 알림

## 주요 기능
1. **소셜 로그인**: 카카오, 애플 OAuth 연동
2. **JWT 기반 인증**: 토큰 발급/재발급
3. **챌린지 시스템**: 사용자 습관 관리
4. **포인트 시스템**: 챌린지 완료 시 포인트 적립
5. **관리자 기능**: 사용자 및 챌린지 관리
6. **슬랙 모니터링**: 에러 및 신규 유저 알림

## 마이그레이션 고려사항

### 의존성 매핑
- Spring Boot Web → Ktor Server
- Spring Data JPA → Exposed ORM 또는 Ktorm
- Spring Security → Ktor Authentication
- QueryDSL → Kotlin SQL DSL
- OpenFeign → Ktor Client
- SpringDoc → Ktor OpenAPI Plugin

### 아키텍처 패턴
현재 계층형 아키텍처 (Controller-Service-Repository)를 유지하되, Kotlin 관례에 맞게 조정

### 보안 고려사항
- JWT 처리 방식
- 소셜 로그인 연동 방식
- CORS 설정
- 인증/인가 미들웨어

## 빌드 및 실행

### 현재 (Gradle)
```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

### 배포 스크립트
- `script/start.sh`: 서버 시작
- `script/stop.sh`: 서버 종료
- `appspec.yml`: AWS CodeDeploy 설정

## 환경 설정
- `local.properties`: 로컬 환경 변수
- MySQL 데이터베이스 연결 필요
- JWT 시크릿 키 설정 필요
- 카카오/애플 OAuth 클라이언트 정보 필요
- 슬랙 웹훅 URL 설정 필요

## 중요 노트
- 현재 프로젝트는 삭제된 README.md가 있음 (git status 확인)
- 테스트 커버리지 확인 필요
- API 문서 호환성 확인 필요
- 데이터베이스 마이그레이션 스크립트 작성 필요

## 마이그레이션 우선순위
1. 핵심 인프라 (서버, 데이터베이스 연결)
2. 인증/인가 시스템
3. 핵심 도메인 (user, challenge)
4. 외부 연동 (소셜 로그인)
5. 부가 기능 (포인트, 관리자, 배너)
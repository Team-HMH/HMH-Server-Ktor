# Spring Boot to Ktor 마이그레이션 가이드

## 📋 프로젝트 개요

### 마이그레이션 대상
- **기존**: Spring Boot 3.2.1 + Java 17
- **새로운**: Ktor + Kotlin
- **프로젝트**: HMH (Habit Making Helper)
- **도메인**: 습관 관리 및 포인트 시스템

### 주요 기술 스택 변화
| 기존 (Spring Boot) | 새로운 (Ktor) |
|-------------------|---------------|
| Spring Boot Web | Ktor Server |
| Spring Data JPA | Exposed ORM |
| Spring Security | Ktor Authentication |
| QueryDSL | Exposed SQL DSL |
| OpenFeign | Ktor Client |
| SpringDoc OpenAPI | Ktor OpenAPI Plugin |

## 🚀 마이그레이션 진행 과정

### 1단계: 핵심 인프라 구축 (이전 세션)
- **Ktor 서버 설정**: Application, Routing, Serialization
- **데이터베이스 연결**: Exposed ORM + MySQL
- **의존성 주입**: Koin DI 프레임워크
- **기본 예외 처리**: BusinessException 체계

### 2단계: 기본 도메인 마이그레이션 (이전 세션)
- **User 도메인**: 사용자 관리 기능
- **Challenge 도메인**: 챌린지 시스템
- **Auth 도메인**: 기본 인증 시스템

---

## 📈 현재 세션 마이그레이션 상세

### 3단계: DailyChallenge 도메인 완성

#### 3.1 Status Enum 수정
```kotlin
// 기존 Java enum
public enum Status {
    SUCCESS, FAIL
}

// 새로운 Kotlin enum (실제 DB와 일치)
enum class Status {
    NONE, FAILURE, EARNED, UNEARNED
}
```

#### 3.2 Repository 구현 (Exposed ORM)
```kotlin
// Spring Data JPA → Exposed ORM
class DailyChallengeRepositoryImpl : DailyChallengeRepository {
    override suspend fun findByUserIdAndChallengeIdAndDate(
        userId: Long, challengeId: Long, date: LocalDate
    ): DailyChallenge? = dbQuery {
        DailyChallenges.select { 
            (DailyChallenges.userId eq userId) and 
            (DailyChallenges.challengeId eq challengeId) and 
            (DailyChallenges.date eq date) 
        }.singleOrNull()?.toDailyChallenge()
    }
}
```

#### 3.3 Service Layer 구현
```kotlin
// 비동기 처리 (suspend functions)
class DailyChallengeService(
    private val dailyChallengeRepository: DailyChallengeRepository,
    private val userService: UserService,
    private val challengeService: ChallengeService
) {
    suspend fun completeChallenge(userId: Long, challengeId: Long): DailyChallengeDto {
        // 비즈니스 로직 구현
    }
}
```

### 4단계: App 도메인 마이그레이션

#### 4.1 Entity 설계
```kotlin
// ChallengeApp - 앱 다운로드/삭제 관리
object ChallengeApps : IntIdTable("challenge_app") {
    val userId = long("user_id")
    val appName = varchar("app_name", 100)
    val goalTime = integer("goal_time")
    val isDeleted = bool("is_deleted").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

// HistoryApp - 앱 사용 히스토리
object HistoryApps : IntIdTable("history_app") {
    val userId = long("user_id")
    val appName = varchar("app_name", 100)
    val usageTime = integer("usage_time")
    val date = date("date")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
```

#### 4.2 API 엔드포인트 구현
```kotlin
// Ktor Routing
fun Route.appRoutes() {
    authenticate("jwt-auth") {
        route("/app") {
            post("/challenge") { /* 챌린지 앱 등록 */ }
            get("/challenge/list") { /* 챌린지 앱 목록 */ }
            delete("/challenge/{id}") { /* 챌린지 앱 삭제 */ }
            post("/history") { /* 앱 사용 히스토리 등록 */ }
            get("/history/list") { /* 히스토리 조회 */ }
        }
    }
}
```

### 5단계: Point 시스템 마이그레이션

#### 5.1 포인트 상수 정의
```kotlin
// 포인트 시스템 규칙
companion object {
    const val EARNED_POINT = 10    // 챌린지 성공 시
    const val USAGE_POINT = 20     // 챌린지 실패 시
}
```

#### 5.2 포인트 서비스 구현
```kotlin
class PointService(
    private val userService: UserService,
    private val dailyChallengeService: DailyChallengeService
) {
    suspend fun earnPoints(userId: Long): PointStatusDto {
        // 포인트 획득 로직
        userService.addPoints(userId, EARNED_POINT)
    }
    
    suspend fun usePoints(userId: Long): PointStatusDto {
        // 포인트 사용 로직
        userService.subtractPoints(userId, USAGE_POINT)
    }
}
```

### 6단계: Exposed SQL DSL 문법 오류 수정

#### 6.1 주요 문법 오류와 해결
```kotlin
// ❌ 잘못된 문법
DailyChallenges.selectAll().where { condition }

// ✅ 올바른 문법
DailyChallenges.select { condition }

// ❌ 잘못된 batchInsert 사용
val ids = batchInsert(items) { ... }
items.forEach { id -> 
    select { table.id eq id } // 추가 쿼리 필요
}

// ✅ 올바른 batchInsert 사용
val results = batchInsert(items) { ... }
results.map { it.toDomain() } // 결과 직접 매핑
```

#### 6.2 DatabaseUtils 함수 올바른 사용
```kotlin
// ❌ 잘못된 import
import sopt.org.hmh.global.common.database.DatabaseUtils.dbQuery

// ✅ 올바른 import
import sopt.org.hmh.global.common.database.dbQuery
```

### 7단계: JWT 예외 시스템 완전 마이그레이션

#### 7.1 Spring Boot JwtError enum → Ktor JwtException sealed class
```kotlin
// 기존 Spring Boot
@AllArgsConstructor
public enum JwtError implements ErrorBase {
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    // ...
}

// 새로운 Ktor
sealed class JwtException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {
    
    data object InvalidAccessToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_ACCESS_TOKEN",
        message = "유효하지 않은 액세스 토큰입니다. 액세스 토큰을 재발급 받아주세요."
    )
    
    data object ExpiredAccessToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "EXPIRED_ACCESS_TOKEN",
        message = "액세스 토큰이 만료되었습니다. 액세스 토큰을 재발급 받아주세요."
    )
}
```

#### 7.2 소셜 로그인 예외 처리 강화
```kotlin
// 카카오 로그인
class KakaoLoginProvider {
    override suspend fun getUserInfo(accessToken: String): SocialUserInfo {
        // ...
        } else {
            throw JwtException.InvalidSocialAccessToken // 구체적인 예외
        }
    }
}

// 애플 로그인
class AppleLoginProvider {
    private suspend fun verifyAndDecodeIdentityToken(identityToken: String): AppleIdTokenPayload {
        try {
            // JWT 검증 로직
        } catch (e: JWTVerificationException) {
            when {
                e.message?.contains("expired") == true -> throw JwtException.ExpiredIdentityToken
                e.message?.contains("claims") == true -> throw JwtException.InvalidIdentityTokenClaims
                else -> throw JwtException.InvalidIdentityToken
            }
        }
    }
}
```

### 8단계: 배포 환경 구성

#### 8.1 Docker 설정
```dockerfile
# 멀티스테이지 빌드
FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle 빌드
RUN ./gradlew build -x test

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

CMD ["./gradlew", "run"]
```

#### 8.2 Docker Compose 전체 스택
```yaml
services:
  hmh-server:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: hmh
      MYSQL_USER: hmh_user
      
  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    ports:
      - "8081:80"
```

## 🔧 핵심 기술 패턴 및 Best Practices

### Exposed ORM 패턴
```kotlin
// Table 정의
object Users : IntIdTable("users") {
    val socialPlatform = enumeration<SocialPlatform>("social_platform")
    val socialId = varchar("social_id", 255)
    val name = varchar("name", 100).nullable()
    val point = integer("point").default(0)
}

// Repository 구현
class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: Long): User? = dbQuery {
        Users.select { Users.id eq id }
            .singleOrNull()?.toUser()
    }
    
    override suspend fun create(/* params */): User = dbQuery {
        val userId = Users.insertAndGetId {
            it[socialPlatform] = socialPlatform
            it[socialId] = socialId
            it[name] = name
        }
        findById(userId.value)!!
    }
}
```

### Koin 의존성 주입
```kotlin
val dataModule = module {
    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<ChallengeRepository> { ChallengeRepositoryImpl() }
    
    // Services
    single { UserService(get()) }
    single { ChallengeService(get()) }
    single { PointService(get(), get()) }
}
```

### Ktor Authentication
```kotlin
fun Application.configureSecurity() {
    if (jwtSecret.isNullOrEmpty()) {
        install(Authentication) {
            jwt("jwt-auth") {} // 빈 설정으로 오류 방지
        }
        return
    }
    
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(jwtProvider.verifier)
            validate { credential ->
                jwtProvider.validateCredential(credential)
            }
        }
    }
}
```

## 📊 마이그레이션 결과

### 완료된 기능
- ✅ **DailyChallenge**: 일일 챌린지 완료/포인트 획득
- ✅ **App**: 앱 관리 및 사용 히스토리 추적
- ✅ **Point**: 포인트 획득/사용 시스템
- ✅ **JWT**: 완전한 예외 처리 시스템
- ✅ **배포**: Docker 기반 배포 환경

### 성능 및 안정성 개선
- **비동기 처리**: Kotlin Coroutines 활용
- **타입 안전성**: Kotlin의 Null Safety
- **메모리 효율성**: Exposed ORM의 최적화된 쿼리
- **예외 처리**: Sealed Class를 통한 컴파일 타임 안전성

### API 호환성 유지
- 기존 Spring Boot API와 동일한 엔드포인트
- 동일한 요청/응답 포맷
- 기존 클라이언트 코드 수정 불필요

## 🎯 학습 포인트

### 1. Framework Migration 전략
- **점진적 마이그레이션**: 도메인별 단계적 진행
- **API 호환성 우선**: 기존 클라이언트 보호
- **테스트 중심**: 각 단계별 검증

### 2. Kotlin/Ktor 활용
- **Coroutines**: 비동기 처리의 우아한 해결
- **Sealed Class**: 타입 안전한 예외 처리
- **Extension Functions**: 코드 재사용성 향상

### 3. ORM Migration
- **Spring Data JPA → Exposed**: SQL 중심적 접근
- **QueryDSL → Exposed DSL**: 타입 안전한 쿼리 작성
- **Repository Pattern**: 일관된 데이터 접근 계층

### 4. 배포 및 운영
- **Docker**: 환경 독립적 배포
- **Health Check**: 서비스 상태 모니터링
- **환경 변수**: 설정 외부화

## 🚀 다음 단계

### 추가 마이그레이션 대상
- **Admin**: 관리자 기능
- **Banner**: 배너 관리
- **Slack**: 알림 시스템

### 성능 최적화
- Connection Pool 튜닝
- 쿼리 최적화
- 캐싱 전략 수립

### 모니터링 및 로깅
- 구조화된 로깅
- 메트릭 수집
- 알람 시스템

---

## 📚 참고 자료

- [Ktor 공식 문서](https://ktor.io/)
- [Exposed ORM 가이드](https://github.com/JetBrains/Exposed)
- [Koin DI 문서](https://insert-koin.io/)
- [Kotlin Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)

---

*이 문서는 실제 마이그레이션 과정을 기록한 것으로, Spring Boot에서 Ktor로의 전환을 고려하는 팀들에게 참고 자료가 되기를 바랍니다.*
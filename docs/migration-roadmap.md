# HMH 프로젝트: Spring Boot → Ktor 마이그레이션 로드맵

## 📋 프로젝트 개요

**목표**: Spring Boot 3.2.1 + Java 17 기반 HMH 서버를 Ktor + Kotlin으로 완전 마이그레이션

**예상 기간**: 8-12주  
**복잡도**: 높음 (인증/보안 시스템의 복잡성으로 인해)  
**주요 위험 요소**: JWT 인증, 소셜 로그인, JPA 연관관계 변환

---

## 🎯 마이그레이션 전략

### 접근 방식
- **Big Bang** ❌ (위험도 높음)
- **점진적 마이그레이션** ✅ (권장)
  - Phase별 독립적 개발 및 테스트
  - 기존 API 호환성 유지
  - 단계별 검증 및 롤백 가능

### 성공 기준
1. **기능적 요구사항**: 모든 기존 API 동작 보장
2. **비기능적 요구사항**: 성능 저하 없음 (응답시간 ±10% 이내)
3. **보안 요구사항**: 기존 JWT/소셜로그인 호환성 100%
4. **운영 요구사항**: 무중단 배포 지원

---

## 📅 Phase별 마이그레이션 계획

### Phase 1: 기반 인프라 구축 (1-2주)

#### 목표
Ktor 프로젝트 기본 골격 구성 및 개발 환경 설정

#### 주요 작업
- [ ] **프로젝트 초기 설정**
  ```kotlin
  // build.gradle.kts
  plugins {
      kotlin("jvm") version "1.9.22"
      kotlin("plugin.serialization") version "1.9.22"
      id("io.ktor.plugin") version "2.3.7"
      id("app.cash.sqldelight") version "2.0.1"
  }
  
  dependencies {
      implementation("io.ktor:ktor-server-core")
      implementation("io.ktor:ktor-server-netty")
      implementation("io.ktor:ktor-server-content-negotiation")
      implementation("io.ktor:ktor-serialization-kotlinx-json")
      implementation("io.ktor:ktor-server-auth")
      implementation("io.ktor:ktor-server-auth-jwt")
      
      // Database
      implementation("org.jetbrains.exposed:exposed-core:0.45.0")
      implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
      implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
      implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")
      implementation("mysql:mysql-connector-java:8.0.33")
      implementation("com.zaxxer:HikariCP:5.0.1")
  }
  ```

- [ ] **환경 설정**
  ```hocon
  // application.conf
  ktor {
      deployment {
          port = 8080
          port = ${?PORT}
      }
      application {
          modules = [ sopt.org.hmh.ApplicationKt.module ]
      }
  }
  
  database {
      url = "jdbc:mysql://localhost:3306/hmh"
      driver = "com.mysql.cj.jdbc.Driver"
      user = ${DB_USER}
      password = ${DB_PASSWORD}
  }
  ```

- [ ] **로깅 및 모니터링 설정**
- [ ] **Docker 컨테이너 설정**
- [ ] **기본 헬스체크 엔드포인트**

#### 완료 기준
- [x] 프로젝트 빌드 성공
- [x] 기본 서버 실행 확인
- [x] 데이터베이스 연결 테스트

---

### Phase 2: 핵심 도메인 마이그레이션 (2-3주)

#### 목표
JPA 엔티티를 Exposed로 변환하고 핵심 도메인 로직 구현

#### 주요 작업

**2.1 엔티티 변환 (1주)**

```kotlin
// User 엔티티 변환
object Users : LongIdTable("users") {
    val name = varchar("name", 255).nullable()
    val currentChallengeId = long("current_challenge_id").nullable()
    val socialPlatform = enumerationByName("social_platform", 10, SocialPlatform::class)
    val socialId = varchar("social_id", 255).uniqueIndex()
    val point = integer("point").default(0)
    val recentLockDate = date("recent_lock_date").nullable()
    val isDeleted = bool("is_deleted").default(false)
    val deletedAt = datetime("deleted_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

data class User(
    val id: Long,
    val name: String?,
    val currentChallengeId: Long?,
    val socialPlatform: SocialPlatform,
    val socialId: String,
    val point: Int,
    val recentLockDate: LocalDate?,
    val isDeleted: Boolean,
    val deletedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun decreasePoint(usagePoint: Int): Int {
        require(point >= usagePoint) { "포인트가 부족합니다." }
        return point - usagePoint
    }
    
    fun increasePoint(earnedPoint: Int): Int = point + earnedPoint
}
```

**2.2 Repository → DAO 변환**

```kotlin
interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findBySocialId(socialId: String): User?
    suspend fun save(user: User): User
    suspend fun delete(id: Long): Boolean
}

class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: Long): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }
    
    override suspend fun findBySocialId(socialId: String): User? = dbQuery {
        Users.selectAll()
            .where { Users.socialId eq socialId }
            .singleOrNull()
            ?.toUser()
    }
    
    override suspend fun save(user: User): User = dbQuery {
        val userId = Users.insertAndGetId {
            it[name] = user.name
            it[socialPlatform] = user.socialPlatform
            it[socialId] = user.socialId
            it[point] = user.point
        }
        findById(userId.value)!!
    }
}

// Extension function for mapping
fun ResultRow.toUser(): User = User(
    id = this[Users.id].value,
    name = this[Users.name],
    currentChallengeId = this[Users.currentChallengeId],
    socialPlatform = this[Users.socialPlatform],
    socialId = this[Users.socialId],
    point = this[Users.point],
    recentLockDate = this[Users.recentLockDate],
    isDeleted = this[Users.isDeleted],
    deletedAt = this[Users.deletedAt],
    createdAt = this[Users.createdAt],
    updatedAt = this[Users.updatedAt]
)
```

**2.3 복잡한 연관관계 처리**

```kotlin
// Challenge와 DailyChallenge 관계 처리
class ChallengeService(private val repository: ChallengeRepository) {
    
    suspend fun getChallengeWithDailyChallenges(challengeId: Long): ChallengeWithDetails? {
        return repository.findById(challengeId)?.let { challenge ->
            val dailyChallenges = getDailyChallengesByChallengeId(challengeId)
            val challengeApps = getChallengeAppsByChallengeId(challengeId)
            
            ChallengeWithDetails(
                challenge = challenge,
                dailyChallenges = dailyChallenges,
                apps = challengeApps
            )
        }
    }
    
    private suspend fun getDailyChallengesByChallengeId(challengeId: Long): List<DailyChallenge> = dbQuery {
        DailyChallenges.selectAll()
            .where { DailyChallenges.challengeId eq challengeId }
            .orderBy(DailyChallenges.challengeDate, SortOrder.ASC)
            .map { it.toDailyChallenge() }
    }
}
```

#### 완료 기준
- [x] 모든 엔티티 변환 완료
- [x] 기본 CRUD 동작 테스트 통과
- [x] 복잡한 연관관계 쿼리 검증

---

### Phase 3: 인증/보안 시스템 (3-4주) ⚠️ **고위험 구간**

#### 목표
Spring Security + JWT를 Ktor Authentication으로 완전 마이그레이션

#### 주요 작업

**3.1 JWT 서비스 구현 (1주)**

```kotlin
class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val accessTokenExpiry: Long = 3600000, // 1시간
    private val refreshTokenExpiry: Long = 1209600000 // 2주
) {
    private val algorithm = Algorithm.HMAC256(secret)
    
    fun generateAccessToken(userId: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withSubject(userId)
        .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
        .withClaim("type", "access")
        .sign(algorithm)
    
    fun generateRefreshToken(userId: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withSubject(userId)
        .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiry))
        .withClaim("type", "refresh")
        .sign(algorithm)
    
    fun verifyToken(token: String): DecodedJWT? = try {
        JWT.require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
            .verify(extractToken(token))
    } catch (e: Exception) {
        null
    }
    
    private fun extractToken(bearerToken: String): String {
        return bearerToken.removePrefix("Bearer ")
    }
}
```

**3.2 Ktor Authentication 설정**

```kotlin
fun Application.configureAuthentication() {
    val jwtService = JwtService(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString()
    )
    
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(jwtService.verifier)
            validate { credential ->
                val userId = credential.payload.subject
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BaseResponse.error<Unit>(
                        code = "UNAUTHORIZED",
                        message = "유효하지 않은 토큰입니다."
                    )
                )
            }
        }
        
        jwt("jwt-refresh") {
            verifier(jwtService.verifier)
            validate { credential ->
                val tokenType = credential.payload.getClaim("type").asString()
                if (tokenType == "refresh") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
```

**3.3 소셜 로그인 구현**

```kotlin
// 카카오 OAuth 클라이언트
class KakaoOAuthClient(private val httpClient: HttpClient) {
    
    suspend fun getAccessToken(code: String): KakaoTokenResponse {
        return httpClient.submitForm(
            url = "https://kauth.kakao.com/oauth/token",
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("client_id", KAKAO_CLIENT_ID)
                append("redirect_uri", KAKAO_REDIRECT_URI)
                append("code", code)
            }
        ).body()
    }
    
    suspend fun getUserInfo(accessToken: String): KakaoUserResponse {
        return httpClient.get("https://kapi.kakao.com/v2/user/me") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }.body()
    }
}

// 애플 Identity Token 검증
class AppleOAuthClient(private val httpClient: HttpClient) {
    
    suspend fun verifyIdentityToken(identityToken: String): AppleUserInfo {
        // 1. 헤더 파싱
        val header = parseTokenHeader(identityToken)
        
        // 2. 애플 공개키 가져오기
        val publicKeys = getApplePublicKeys()
        
        // 3. 적절한 공개키 선택 및 검증
        val publicKey = selectPublicKey(header, publicKeys)
        
        // 4. 토큰 검증 및 클레임 추출
        return verifyAndExtractClaims(identityToken, publicKey)
    }
    
    private suspend fun getApplePublicKeys(): ApplePublicKeysResponse {
        return httpClient.get("https://appleid.apple.com/auth/keys").body()
    }
}
```

**3.4 인증 라우팅 구현**

```kotlin
fun Route.authRoutes(
    authService: AuthService,
    jwtService: JwtService
) {
    route("/api/v1/auth") {
        
        post("/login") {
            val request = call.receive<SocialPlatformRequest>()
            val socialAccessToken = call.request.header("Authorization")
                ?: throw AuthException("소셜 액세스 토큰이 필요합니다.")
            
            val response = authService.socialLogin(socialAccessToken, request.socialPlatform)
            call.respond(HttpStatusCode.OK, BaseResponse.success(response))
        }
        
        post("/signup") {
            val request = call.receive<SocialSignUpRequest>()
            val socialAccessToken = call.request.header("Authorization")
                ?: throw AuthException("소셜 액세스 토큰이 필요합니다.")
            val os = call.request.header("OS") ?: "UNKNOWN"
            val timeZone = call.request.header("Time-Zone") ?: "UTC"
            
            val response = authService.socialSignUp(socialAccessToken, request, os, timeZone)
            call.respond(HttpStatusCode.OK, BaseResponse.success(response))
        }
        
        authenticate("jwt-refresh") {
            post("/reissue") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.subject
                
                val response = authService.reissueToken(userId)
                call.respond(HttpStatusCode.OK, BaseResponse.success(response))
            }
        }
    }
}
```

#### 완료 기준
- [x] JWT 토큰 발급/검증 동작
- [x] 소셜 로그인 (카카오/애플) 연동 테스트
- [x] 기존 토큰과의 호환성 검증
- [x] 보안 테스트 (토큰 위변조, 만료 등)

---

### Phase 4: API 엔드포인트 변환 (2-3주)

#### 목표
Spring Controller를 Ktor Routing으로 변환

#### 주요 작업

**4.1 라우팅 구조 설계**

```kotlin
fun Application.configureRouting() {
    routing {
        // 헬스체크 (인증 불필요)
        get("/health") {
            call.respond(HttpStatusCode.OK, "OK")
        }
        
        // 인증이 필요한 라우트
        authenticate("jwt-auth") {
            userRoutes()
            challengeRoutes()
            dailyChallengeRoutes()
            pointRoutes()
            bannerRoutes()
        }
        
        // 관리자 라우트
        authenticate("jwt-admin") {
            adminRoutes()
        }
        
        // 인증이 불필요한 라우트
        authRoutes()
    }
}
```

**4.2 컨트롤러 변환 예시**

```kotlin
// Before: Spring Controller
@RestController
@RequestMapping("/api/v1/challenge")
class ChallengeController(private val challengeFacade: ChallengeFacade) {
    
    @PostMapping
    fun createChallenge(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ChallengeRequest
    ): ResponseEntity<BaseResponse<ChallengeResponse>> {
        val response = challengeFacade.createChallenge(userId, request)
        return ResponseEntity.ok(BaseResponse.success(response))
    }
}

// After: Ktor Route
fun Route.challengeRoutes(challengeService: ChallengeService) {
    route("/api/v1/challenge") {
        
        post {
            val userId = call.userId() // Extension function
            val request = call.receive<ChallengeRequest>()
            
            val response = challengeService.createChallenge(userId, request)
            call.respond(HttpStatusCode.OK, BaseResponse.success(response))
        }
        
        get("/{id}") {
            val challengeId = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("잘못된 챌린지 ID입니다.")
            val userId = call.userId()
            
            val response = challengeService.getChallenge(userId, challengeId)
            call.respond(HttpStatusCode.OK, BaseResponse.success(response))
        }
        
        put("/{id}") {
            val challengeId = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("잘못된 챌린지 ID입니다.")
            val userId = call.userId()
            val request = call.receive<ChallengeUpdateRequest>()
            
            val response = challengeService.updateChallenge(userId, challengeId, request)
            call.respond(HttpStatusCode.OK, BaseResponse.success(response))
        }
    }
}

// 헬퍼 확장 함수
fun ApplicationCall.userId(): Long {
    val principal = principal<JWTPrincipal>()
        ?: throw UnauthorizedException("인증이 필요합니다.")
    return principal.payload.subject.toLong()
}
```

**4.3 예외 처리 시스템**

```kotlin
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AuthenticationException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                BaseResponse.error<Unit>(
                    code = "UNAUTHORIZED",
                    message = cause.message ?: "인증에 실패했습니다."
                )
            )
        }
        
        exception<AuthorizationException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                BaseResponse.error<Unit>(
                    code = "FORBIDDEN",
                    message = cause.message ?: "권한이 부족합니다."
                )
            )
        }
        
        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                BaseResponse.error<Unit>(
                    code = "BAD_REQUEST",
                    message = cause.message ?: "잘못된 요청입니다."
                )
            )
        }
        
        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                BaseResponse.error<Unit>(
                    code = "NOT_FOUND",
                    message = cause.message ?: "리소스를 찾을 수 없습니다."
                )
            )
        }
        
        exception<Throwable> { call, cause ->
            // 슬랙 알림 발송
            slackSender.sendErrorNotification(cause)
            
            call.respond(
                HttpStatusCode.InternalServerError,
                BaseResponse.error<Unit>(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "서버 내부 오류가 발생했습니다."
                )
            )
        }
    }
}
```

#### 완료 기준
- [x] 모든 API 엔드포인트 변환 완료
- [x] 기존 API와 동일한 요청/응답 형식 유지
- [x] 예외 처리 및 에러 응답 검증

---

### Phase 5: 외부 연동 시스템 (1-2주)

#### 목표
OpenFeign → Ktor Client, Slack API 연동

#### 주요 작업

**5.1 HTTP 클라이언트 설정**

```kotlin
fun Application.configureHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }
        
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
```

**5.2 Slack 알림 시스템**

```kotlin
class SlackService(private val httpClient: HttpClient) {
    
    suspend fun sendErrorNotification(error: Throwable, requestInfo: String? = null) {
        val webhookUrl = System.getenv("SLACK_ERROR_WEBHOOK_URL") ?: return
        
        val message = SlackMessage(
            text = "🚨 *HMH 서버 에러 발생*",
            attachments = listOf(
                SlackAttachment(
                    color = "danger",
                    fields = listOf(
                        SlackField("에러 타입", error::class.simpleName ?: "Unknown", true),
                        SlackField("에러 메시지", error.message ?: "No message", true),
                        SlackField("요청 정보", requestInfo ?: "Unknown", false),
                        SlackField("발생 시간", Instant.now().toString(), true)
                    )
                )
            )
        )
        
        try {
            httpClient.post(webhookUrl) {
                setBody(message)
            }
        } catch (e: Exception) {
            println("슬랙 알림 전송 실패: ${e.message}")
        }
    }
    
    suspend fun sendNewUserNotification(user: User) {
        val webhookUrl = System.getenv("SLACK_NEW_USER_WEBHOOK_URL") ?: return
        
        val message = SlackMessage(
            text = "🎉 *신규 회원 가입*",
            attachments = listOf(
                SlackAttachment(
                    color = "good",
                    fields = listOf(
                        SlackField("사용자 ID", user.id.toString(), true),
                        SlackField("소셜 플랫폼", user.socialPlatform.name, true),
                        SlackField("가입 시간", user.createdAt.toString(), true)
                    )
                )
            )
        )
        
        httpClient.post(webhookUrl) {
            setBody(message)
        }
    }
}
```

#### 완료 기준
- [x] HTTP 클라이언트 정상 동작
- [x] 슬랙 알림 테스트 성공
- [x] 외부 API 호출 성능 검증

---

## 🧪 테스트 전략

### 테스트 단계별 접근

**1. 단위 테스트**
```kotlin
class UserServiceTest {
    
    @Test
    fun `사용자 생성 테스트`() = testApplication {
        // Given
        val user = User(...)
        
        // When
        val result = userService.createUser(user)
        
        // Then
        assertEquals(user.socialId, result.socialId)
    }
}
```

**2. 통합 테스트**
```kotlin
class AuthIntegrationTest {
    
    @Test
    fun `소셜 로그인 통합 테스트`() = testApplication {
        application {
            configureAuthentication()
            configureRouting()
        }
        
        client.post("/api/v1/auth/login") {
            header("Authorization", "Bearer test-token")
            setBody(SocialPlatformRequest(SocialPlatform.KAKAO))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
```

**3. API 호환성 테스트**
- 기존 클라이언트 앱과의 호환성 검증
- 요청/응답 스키마 일치성 확인
- 에러 코드 및 메시지 일관성 검증

---

## 🚀 배포 및 운영

### 배포 전략

**1. 카나리 배포**
- 소량 트래픽으로 신규 Ktor 서버 검증
- 문제 발생 시 즉시 롤백

**2. 블루-그린 배포**
- 기존 Spring Boot (Blue)와 신규 Ktor (Green) 병렬 운영
- 로드밸런서 설정으로 점진적 트래픽 이전

### 모니터링

**필수 메트릭**
- 응답 시간 (평균, P95, P99)
- 에러율 (4xx, 5xx)
- 처리량 (RPS)
- 메모리 및 CPU 사용률

**알림 설정**
- 에러율 5% 초과 시 즉시 알림
- 응답시간 P95가 1초 초과 시 경고
- 메모리 사용률 80% 초과 시 알림

---

## ⚠️ 위험 요소 및 완화 방안

### 주요 위험 요소

| 위험 요소 | 영향도 | 확률 | 완화 방안 |
|-----------|--------|------|-----------|
| JWT 토큰 호환성 문제 | 높음 | 중간 | 철저한 테스트, 토큰 형식 호환성 유지 |
| 소셜 로그인 연동 오류 | 높음 | 중간 | 단계별 검증, 풀백 API 준비 |
| 성능 저하 | 중간 | 낮음 | 성능 테스트, 최적화 작업 |
| 데이터 불일치 | 높음 | 낮음 | 마이그레이션 스크립트 검증 |

### 롤백 계획

**즉시 롤백 조건**
- 에러율 10% 초과
- 응답시간 2배 이상 증가
- 데이터 무결성 문제 발견

**롤백 절차**
1. 로드밸런서에서 Ktor 서버 제외
2. Spring Boot 서버로 모든 트래픽 라우팅
3. 문제 원인 분석 및 수정
4. 재배포 검토

---

## 📋 체크리스트

### Phase별 완료 체크리스트

#### Phase 1: 기반 인프라
- [ ] Gradle 빌드 스크립트 작성
- [ ] 환경 설정 파일 구성
- [ ] 데이터베이스 연결 확인
- [ ] Docker 컨테이너 설정
- [ ] 기본 헬스체크 API

#### Phase 2: 도메인 마이그레이션
- [ ] User 엔티티 변환
- [ ] Challenge 엔티티 변환
- [ ] DailyChallenge 엔티티 변환
- [ ] Repository 레이어 구현
- [ ] 기본 CRUD 테스트

#### Phase 3: 인증/보안
- [ ] JWT 서비스 구현
- [ ] Ktor Authentication 설정
- [ ] 카카오 OAuth 연동
- [ ] 애플 로그인 연동
- [ ] 보안 테스트 완료

#### Phase 4: API 변환
- [ ] 모든 엔드포인트 변환
- [ ] 예외 처리 시스템
- [ ] API 문서 업데이트
- [ ] 호환성 테스트

#### Phase 5: 외부 연동
- [ ] HTTP 클라이언트 설정
- [ ] 슬랙 알림 연동
- [ ] 외부 API 테스트
- [ ] 모니터링 설정

### 최종 검증 체크리스트
- [ ] 전체 기능 테스트 통과
- [ ] 성능 테스트 기준 충족
- [ ] 보안 취약점 점검 완료
- [ ] 운영 환경 배포 테스트
- [ ] 모니터링 및 알림 동작 확인
- [ ] 롤백 계획 검증 완료

---

## 📚 참고 자료

- [Ktor 공식 문서](https://ktor.io/docs/)
- [Exposed ORM 가이드](https://github.com/JetBrains/Exposed)
- [Kotlin 마이그레이션 베스트 프랙티스](https://kotlinlang.org/docs/mixing-java-kotlin-intellij.html)
- [JWT 보안 가이드](https://auth0.com/blog/a-look-at-the-latest-draft-for-jwt-bcp/)

---

## 👥 팀 역할 및 책임

| 역할 | 담당자 | 주요 책임 |
|------|--------|-----------|
| 프로젝트 리드 | TBD | 전체 일정 관리, 아키텍처 설계 |
| 백엔드 개발자 | TBD | 도메인 로직, API 구현 |
| 보안 전문가 | TBD | 인증/보안 시스템 설계 |
| DevOps 엔지니어 | TBD | 배포, 모니터링 설정 |
| QA 엔지니어 | TBD | 테스트 계획, 품질 검증 |

---

**문서 버전**: 1.0  
**최종 수정일**: 2024-08-03  
**작성자**: Claude Code Assistant
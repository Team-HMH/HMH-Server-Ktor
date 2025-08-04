package sopt.org.hmh.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.response.*
import sopt.org.hmh.global.common.response.BaseResponse
import sopt.org.hmh.global.auth.jwt.JwtProvider

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString()
    
    if (jwtSecret.isNullOrEmpty()) {
        println("⚠️ JWT 설정이 불완전합니다. 인증 기능은 비활성화됩니다.")
        // Authentication 플러그인은 설치하되 빈 설정으로 유지
        install(Authentication) {
            // 빈 설정
            jwt("jwt-auth") {}
            jwt("jwt-refresh") {}
            jwt("jwt-admin") {}
        }
        return
    }
    
    install(Authentication) {
        jwt("jwt-auth") {
            realm = "HMH Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer("hmh-server")
                    .build()
            )
            validate { credential ->
                try {
                    val userId = credential.payload.getClaim(JwtProvider.USER_CLAIM).asLong()
                    val role = credential.payload.getClaim(JwtProvider.ROLE_CLAIM).asString()
                    
                    if (userId != null && role == "USER") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BaseResponse.error<Unit>(
                        code = "JWT_INVALID_TOKEN",
                        message = "유효하지 않은 토큰입니다."
                    )
                )
            }
        }
        
        jwt("jwt-refresh") {
            realm = "HMH Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer("hmh-server")
                    .build()
            )
            validate { credential ->
                try {
                    val userId = credential.payload.getClaim(JwtProvider.USER_CLAIM).asLong()
                    val role = credential.payload.getClaim(JwtProvider.ROLE_CLAIM).asString()
                    
                    if (userId != null && role == "USER") {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BaseResponse.error<Unit>(
                        code = "JWT_REFRESH_TOKEN_INVALID",
                        message = "유효하지 않은 리프레시 토큰입니다."
                    )
                )
            }
        }
        
        jwt("jwt-admin") {
            realm = "HMH Server Admin"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer("hmh-server")
                    .build()
            )
            validate { credential ->
                try {
                    val role = credential.payload.getClaim(JwtProvider.ROLE_CLAIM).asString()
                    val adminId = credential.payload.getClaim("adminId").asString()
                    
                    if (role == "ADMIN" && adminId != null) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Forbidden,
                    BaseResponse.error<Unit>(
                        code = "JWT_ADMIN_TOKEN_REQUIRED",
                        message = "관리자 토큰이 필요합니다."
                    )
                )
            }
        }
    }
}
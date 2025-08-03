package sopt.org.hmh.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.response.*
import sopt.org.hmh.global.common.response.BaseResponse

fun Application.configureSecurity() {
    val config = environment.config
    val secret = config.propertyOrNull("jwt.secret")?.getString()
    val issuer = config.propertyOrNull("jwt.issuer")?.getString()
    val audience = config.propertyOrNull("jwt.audience")?.getString()
    
    if (secret.isNullOrEmpty() || issuer.isNullOrEmpty() || audience.isNullOrEmpty()) {
        println("⚠️ JWT 설정이 불완전합니다. 인증 기능은 비활성화됩니다.")
        return
    }
    
    install(Authentication) {
        jwt("jwt-auth") {
            realm = "HMH Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
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
            realm = "HMH Server"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val tokenType = credential.payload.getClaim("type").asString()
                if (tokenType == "refresh") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
        
        jwt("jwt-admin") {
            realm = "HMH Server Admin"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val userRole = credential.payload.getClaim("role").asString()
                if (userRole == "admin") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
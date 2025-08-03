package sopt.org.hmh.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import sopt.org.hmh.global.common.response.BaseResponse
import sopt.org.hmh.global.common.exception.BusinessException
import sopt.org.hmh.domain.user.controller.userRoutes

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is BusinessException -> {
                    call.respond(
                        cause.status,
                        BaseResponse.error<Unit>(
                            code = cause.errorCode,
                            message = cause.message ?: "비즈니스 로직 오류가 발생했습니다."
                        )
                    )
                }
                else -> {
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
    }

    routing {
        // 헬스체크 엔드포인트
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                BaseResponse.success(mapOf("status" to "OK", "timestamp" to System.currentTimeMillis()))
            )
        }
        
        get("/health/ready") {
            // 데이터베이스 연결 상태 확인 등
            call.respond(
                HttpStatusCode.OK,
                BaseResponse.success(mapOf("status" to "READY"))
            )
        }
        
        get("/health/live") {
            call.respond(
                HttpStatusCode.OK,
                BaseResponse.success(mapOf("status" to "ALIVE"))
            )
        }
        
        // API 라우팅
        route("/api/v1") {
            // 인증이 필요하지 않은 라우트 (테스트용)
            userRoutes()
            
            // 인증이 필요한 라우트 (JWT 설정 완료 후 활성화 예정)
            // authenticate("jwt-auth") {
            //     userRoutes()
            //     challengeRoutes()
            // }
            
            // 관리자 전용 라우트 (JWT 설정 완료 후 활성화 예정)
            // authenticate("jwt-admin") {
            //     adminRoutes()
            // }
        }
    }
}
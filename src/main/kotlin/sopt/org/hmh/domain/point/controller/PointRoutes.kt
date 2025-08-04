package sopt.org.hmh.domain.point.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import sopt.org.hmh.domain.point.dto.*
import sopt.org.hmh.domain.point.service.PointService
import sopt.org.hmh.global.auth.jwt.JwtProvider
import sopt.org.hmh.global.common.response.BaseResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.pointRoutes() {
    val pointService by inject<PointService>()

    route("/point") {
        authenticate("jwt-auth") {
            
            // 챌린지 포인트 상태 목록 조회
            get("/list") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@get call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val response = pointService.getChallengePointStatusList(userId)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BaseResponse.error<Unit>(
                            code = "POINT_LIST_FETCH_FAILED",
                            message = e.message ?: "포인트 목록 조회에 실패했습니다."
                        )
                    )
                }
            }

            // 포인트 획득 (챌린지 성공)
            patch("/earn") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@patch call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<ChallengeDateRequest>()
                    val challengeDate = LocalDate.parse(request.challengeDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    val response = pointService.earnPointAndChallengeEarned(userId, challengeDate)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "POINT_EARN_FAILED",
                            message = e.message ?: "포인트 획득에 실패했습니다."
                        )
                    )
                }
            }

            // 포인트 사용 (챌린지 실패) - v1 (Deprecated)
            patch("/use") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@patch call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<ChallengeDateRequest>()
                    val challengeDate = LocalDate.parse(request.challengeDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    val response = pointService.usePointAndChallengeFailedDeprecated(userId, challengeDate)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "POINT_USE_FAILED",
                            message = e.message ?: "포인트 사용에 실패했습니다."
                        )
                    )
                }
            }

            // 획득 가능한 포인트 정보 조회
            get("/earn") {
                try {
                    val response = pointService.getEarnedPoint()
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BaseResponse.error<Unit>(
                            code = "EARNED_POINT_INFO_FETCH_FAILED",
                            message = e.message ?: "획득 포인트 정보 조회에 실패했습니다."
                        )
                    )
                }
            }

            // 사용 포인트 정보 조회
            get("/use") {
                try {
                    val response = pointService.getUsagePoint()
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BaseResponse.error<Unit>(
                            code = "USAGE_POINT_INFO_FETCH_FAILED",
                            message = e.message ?: "사용 포인트 정보 조회에 실패했습니다."
                        )
                    )
                }
            }
        }
    }
    
    // v2 API
    route("/v2/point") {
        authenticate("jwt-auth") {
            
            // 포인트 사용 (오늘의 챌린지 실패) - v2
            patch("/use") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@patch call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val response = pointService.usePointAndTodayDailyChallengeFailed(userId)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "POINT_USE_FAILED",
                            message = e.message ?: "포인트 사용에 실패했습니다."
                        )
                    )
                }
            }
        }
    }
}
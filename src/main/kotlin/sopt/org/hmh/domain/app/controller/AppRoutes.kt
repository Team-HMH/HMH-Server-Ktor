package sopt.org.hmh.domain.app.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import sopt.org.hmh.domain.app.dto.*
import sopt.org.hmh.domain.app.service.ChallengeAppService
import sopt.org.hmh.domain.app.service.HistoryAppService
import sopt.org.hmh.global.auth.jwt.JwtProvider
import sopt.org.hmh.global.common.response.BaseResponse

fun Route.appRoutes() {
    val challengeAppService by inject<ChallengeAppService>()
    val historyAppService by inject<HistoryAppService>()

    route("/app") {
        authenticate("jwt-auth") {
            
            // 챌린지 앱 관리
            route("/challenge") {
                
                // 단일 챌린지 앱 추가
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@post call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val request = call.receive<ChallengeAppRequest>()
                        val challengeApp = challengeAppService.addChallengeApp(
                            challengeId = request.challengeId,
                            appCode = request.appCode,
                            goalTime = request.goalTime,
                            os = request.os
                        )
                        
                        val response = ChallengeAppResponse.from(challengeApp)
                        
                        call.respond(
                            HttpStatusCode.Created,
                            BaseResponse.success(response)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BaseResponse.error<Unit>(
                                code = "CHALLENGE_APP_CREATE_FAILED",
                                message = e.message ?: "챌린지 앱 추가에 실패했습니다."
                            )
                        )
                    }
                }

                // 여러 챌린지 앱 추가
                post("/batch") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@post call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val request = call.receive<ChallengeAppArrayRequest>()
                        val challengeApps = challengeAppService.addChallengeApps(
                            challengeId = request.challengeId,
                            apps = request.apps,
                            os = call.request.headers["X-OS"] ?: "unknown"
                        )
                        
                        val responses = challengeApps.map { ChallengeAppResponse.from(it) }
                        
                        call.respond(
                            HttpStatusCode.Created,
                            BaseResponse.success(responses)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BaseResponse.error<Unit>(
                                code = "CHALLENGE_APP_BATCH_CREATE_FAILED",
                                message = e.message ?: "챌린지 앱 일괄 추가에 실패했습니다."
                            )
                        )
                    }
                }

                // 챌린지별 앱 목록 조회
                get("/{challengeId}") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@get call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val challengeId = call.parameters["challengeId"]?.toLongOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                BaseResponse.error<Unit>("INVALID_CHALLENGE_ID", "유효하지 않은 챌린지 ID입니다.")
                            )
                        
                        val challengeApps = challengeAppService.getChallengeAppsByChallengeId(challengeId)
                        val responses = challengeApps.map { ChallengeAppResponse.from(it) }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success(responses)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BaseResponse.error<Unit>(
                                code = "CHALLENGE_APP_FETCH_FAILED",
                                message = e.message ?: "챌린지 앱 조회에 실패했습니다."
                            )
                        )
                    }
                }

                // 챌린지 앱 삭제
                delete {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@delete call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val request = call.receive<AppRemoveRequest>()
                        val removed = challengeAppService.removeChallengeApp(
                            challengeId = request.challengeId,
                            appCode = request.appCode
                        )
                        
                        if (removed) {
                            call.respond(
                                HttpStatusCode.OK,
                                BaseResponse.success("챌린지 앱이 삭제되었습니다.")
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                BaseResponse.error<Unit>("CHALLENGE_APP_NOT_FOUND", "챌린지 앱을 찾을 수 없습니다.")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BaseResponse.error<Unit>(
                                code = "CHALLENGE_APP_DELETE_FAILED",
                                message = e.message ?: "챌린지 앱 삭제에 실패했습니다."
                            )
                        )
                    }
                }
            }

            // 앱 사용 기록 관리
            route("/history") {
                
                // 앱 사용 기록 추가
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@post call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val request = call.receive<HistoryAppRequest>()
                        val historyApp = historyAppService.addHistoryApp(
                            dailyChallengeId = request.dailyChallengeId,
                            appCode = request.appCode,
                            goalTime = request.goalTime,
                            usageTime = request.usageTime,
                            os = request.os
                        )
                        
                        val response = HistoryAppResponse.from(historyApp)
                        
                        call.respond(
                            HttpStatusCode.Created,
                            BaseResponse.success(response)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BaseResponse.error<Unit>(
                                code = "HISTORY_APP_CREATE_FAILED",
                                message = e.message ?: "앱 사용 기록 추가에 실패했습니다."
                            )
                        )
                    }
                }

                // 일일 챌린지별 앱 사용 기록 조회
                get("/{dailyChallengeId}") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@get call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val dailyChallengeId = call.parameters["dailyChallengeId"]?.toLongOrNull()
                            ?: return@get call.respond(
                                HttpStatusCode.BadRequest,
                                BaseResponse.error<Unit>("INVALID_DAILY_CHALLENGE_ID", "유효하지 않은 일일 챌린지 ID입니다.")
                            )
                        
                        val historyApps = historyAppService.getHistoryAppsByDailyChallengeId(dailyChallengeId)
                        val responses = historyApps.map { HistoryAppResponse.from(it) }
                        
                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success(responses)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BaseResponse.error<Unit>(
                                code = "HISTORY_APP_FETCH_FAILED",
                                message = e.message ?: "앱 사용 기록 조회에 실패했습니다."
                            )
                        )
                    }
                }
            }
        }
    }
}
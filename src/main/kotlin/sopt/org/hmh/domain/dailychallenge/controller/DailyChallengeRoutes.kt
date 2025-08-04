package sopt.org.hmh.domain.dailychallenge.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import sopt.org.hmh.domain.dailychallenge.dto.*
import sopt.org.hmh.domain.dailychallenge.service.DailyChallengeService
import sopt.org.hmh.global.auth.jwt.JwtProvider
import sopt.org.hmh.global.common.response.BaseResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Route.dailyChallengeRoutes() {
    val dailyChallengeService by inject<DailyChallengeService>()

    route("/challenge/daily") {
        authenticate("jwt-auth") {
            
            // 일일 챌린지 완료 처리
            post("/finish") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<FinishedDailyChallengeListRequest>()
                    val currentDate = LocalDate.parse(request.currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    val finishedChallenges = dailyChallengeService.finishDailyChallengeList(
                        userId, 
                        request.challengeIdList, 
                        currentDate
                    )
                    
                    val challengeStatuses = finishedChallenges.map { 
                        ChallengeStatusResponse(
                            challengeId = it.challengeId,
                            status = it.status.name,
                            isFinished = it.status != sopt.org.hmh.domain.dailychallenge.entity.Status.NONE
                        )
                    }
                    
                    val response = ChallengeStatusesResponse(challengeStatuses)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "DAILY_CHALLENGE_FINISH_FAILED",
                            message = e.message ?: "일일 챌린지 완료 처리에 실패했습니다."
                        )
                    )
                }
            }

            // 일일 챌린지 상태 변경 (성공/실패)
            post("/success") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<FinishedDailyChallengeStatusListRequest>()
                    val status = dailyChallengeService.parseStatus(request.status)
                    
                    val updatedCount = dailyChallengeService.updateDailyChallengeStatusList(
                        userId, 
                        request.challengeIdList, 
                        status
                    )
                    
                    val challengeStatuses = request.challengeIdList.map { challengeId ->
                        ChallengeStatusResponse(
                            challengeId = challengeId,
                            status = status.name,
                            isFinished = status != sopt.org.hmh.domain.dailychallenge.entity.Status.NONE
                        )
                    }
                    
                    val response = ChallengeStatusesResponse(challengeStatuses)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "DAILY_CHALLENGE_STATUS_UPDATE_FAILED",
                            message = e.message ?: "일일 챌린지 상태 변경에 실패했습니다."
                        )
                    )
                }
            }

            // 단일 일일 챌린지 완료 처리
            post("/finish/single") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<FinishedDailyChallengeRequest>()
                    val currentDate = LocalDate.parse(request.currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    val finishedChallenge = dailyChallengeService.finishDailyChallenge(
                        userId, 
                        request.challengeId, 
                        currentDate
                    )
                    
                    val response = DailyChallengeResponse.from(finishedChallenge)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(response)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "DAILY_CHALLENGE_FINISH_FAILED",
                            message = e.message ?: "일일 챌린지 완료 처리에 실패했습니다."
                        )
                    )
                }
            }

            // 단일 일일 챌린지 상태 변경
            post("/success/single") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                        ?: return@post call.respond(
                            HttpStatusCode.Unauthorized,
                            BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                        )

                    val request = call.receive<FinishedDailyChallengeStatusRequest>()
                    val status = dailyChallengeService.parseStatus(request.status)
                    
                    val updated = dailyChallengeService.updateDailyChallengeStatus(
                        userId, 
                        request.challengeId, 
                        status
                    )
                    
                    if (updated) {
                        val response = ChallengeStatusResponse(
                            challengeId = request.challengeId,
                            status = status.name,
                            isFinished = status != sopt.org.hmh.domain.dailychallenge.entity.Status.NONE
                        )
                        
                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success(response)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            BaseResponse.error<Unit>(
                                code = "DAILY_CHALLENGE_NOT_FOUND",
                                message = "일일 챌린지를 찾을 수 없습니다."
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        BaseResponse.error<Unit>(
                            code = "DAILY_CHALLENGE_STATUS_UPDATE_FAILED",
                            message = e.message ?: "일일 챌린지 상태 변경에 실패했습니다."
                        )
                    )
                }
            }

            // 사용자의 일일 챌린지 목록 조회
            get("/user/{challengeId}") {
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
                    
                    val dailyChallenges = dailyChallengeService.findDailyChallengesByUserIdAndChallengeId(userId, challengeId)
                    val responses = dailyChallenges.map { DailyChallengeResponse.from(it) }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(responses)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BaseResponse.error<Unit>(
                            code = "DAILY_CHALLENGE_FETCH_FAILED",
                            message = e.message ?: "일일 챌린지 조회에 실패했습니다."
                        )
                    )
                }
            }
        }
    }
}
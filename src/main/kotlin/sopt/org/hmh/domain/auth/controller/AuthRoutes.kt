package sopt.org.hmh.domain.auth.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import sopt.org.hmh.domain.auth.dto.*
import sopt.org.hmh.domain.auth.service.AuthService
import sopt.org.hmh.global.auth.jwt.JwtProvider
import sopt.org.hmh.global.common.response.BaseResponse

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/auth") {

        // 소셜 로그인
        post("/login") {
            try {
                val request = call.receive<SocialLoginRequestDto>()
                val loginResponse = authService.socialLogin(request.toSocialLoginRequest())

                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse.success(loginResponse)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>(
                        code = "AUTH_LOGIN_FAILED",
                        message = e.message ?: "로그인에 실패했습니다."
                    )
                )
            }
        }

        // 토큰 재발급
        post("/reissue") {
            try {
                val request = call.receive<TokenReissueRequest>()
                val reissueResponse = authService.reissueToken(request.refreshToken)

                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse.success(reissueResponse)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    BaseResponse.error<Unit>(
                        code = "AUTH_REISSUE_FAILED",
                        message = e.message ?: "토큰 재발급에 실패했습니다."
                    )
                )
            }
        }

        // 인증이 필요한 라우트들 (JWT 설정이 있을 때만 활성화)
        try {
            authenticate("jwt-auth") {

                // 로그아웃
                post("/logout") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@post call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        authService.logout(userId)

                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success("로그아웃되었습니다.")
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BaseResponse.error<Unit>(
                                code = "AUTH_LOGOUT_FAILED",
                                message = e.message ?: "로그아웃에 실패했습니다."
                            )
                        )
                    }
                }

                // 회원 탈퇴
                delete("/withdraw") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@delete call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val result = authService.withdrawUser(userId)

                        if (result) {
                            call.respond(
                                HttpStatusCode.OK,
                                BaseResponse.success("회원 탈퇴가 완료되었습니다.")
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                BaseResponse.error<Unit>("AUTH_WITHDRAW_FAILED", "회원 탈퇴에 실패했습니다.")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            BaseResponse.error<Unit>(
                                code = "AUTH_WITHDRAW_FAILED",
                                message = e.message ?: "회원 탈퇴에 실패했습니다."
                            )
                        )
                    }
                }

                // 내 프로필 조회
                get("/me") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@get call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val userInfo = authService.getMyProfile(userId)

                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success(userInfo)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            BaseResponse.error<Unit>(
                                code = "AUTH_USER_NOT_FOUND",
                                message = e.message ?: "사용자를 찾을 수 없습니다."
                            )
                        )
                    }
                }

                // 프로필 수정
                put("/me") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.getClaim(JwtProvider.USER_CLAIM, Long::class)
                            ?: return@put call.respond(
                                HttpStatusCode.Unauthorized,
                                BaseResponse.error<Unit>("AUTH_INVALID_TOKEN", "유효하지 않은 토큰입니다.")
                            )

                        val request = call.receive<ProfileUpdateRequest>()
                        val updatedUserInfo = authService.updateProfile(userId, request)

                        call.respond(
                            HttpStatusCode.OK,
                            BaseResponse.success(updatedUserInfo)
                        )
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            BaseResponse.error<Unit>(
                                code = "AUTH_PROFILE_UPDATE_FAILED",
                                message = e.message ?: "프로필 수정에 실패했습니다."
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // JWT 설정이 없으면 인증 라우트 비활성화
        }
    }
}
package sopt.org.hmh.domain.user.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import sopt.org.hmh.domain.user.dto.*
import sopt.org.hmh.domain.user.service.UserService
import sopt.org.hmh.global.common.response.BaseResponse

fun Route.userRoutes() {
    val userService by inject<UserService>()

    route("/users") {
        
        // 사용자 조회
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", "잘못된 사용자 ID입니다.")
                )

            try {
                val user = userService.getUserById(id)
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse.success(UserResponse.from(user))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.NotFound,
                    BaseResponse.error<Unit>("NOT_FOUND", e.message ?: "사용자를 찾을 수 없습니다.")
                )
            }
        }

        // 사용자 생성
        post {
            try {
                val request = call.receive<UserCreateRequest>()
                val user = userService.createUser(
                    socialPlatform = request.socialPlatform,
                    socialId = request.socialId,
                    name = request.name
                )
                call.respond(
                    HttpStatusCode.Created,
                    BaseResponse.success(UserResponse.from(user))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", e.message ?: "사용자 생성에 실패했습니다.")
                )
            }
        }

        // 사용자 정보 수정
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", "잘못된 사용자 ID입니다.")
                )

            try {
                val request = call.receive<UserUpdateRequest>()
                request.name?.let { name ->
                    val user = userService.updateUserName(id, name)
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success(UserResponse.from(user))
                    )
                } ?: call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", "수정할 정보가 없습니다.")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", e.message ?: "사용자 정보 수정에 실패했습니다.")
                )
            }
        }

        // 사용자 삭제 (소프트 딜리트)
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", "잘못된 사용자 ID입니다.")
                )

            try {
                val result = userService.softDeleteUser(id)
                if (result) {
                    call.respond(
                        HttpStatusCode.OK,
                        BaseResponse.success("사용자가 삭제되었습니다.")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        BaseResponse.error<Unit>("INTERNAL_ERROR", "사용자 삭제에 실패했습니다.")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    BaseResponse.error<Unit>("BAD_REQUEST", e.message ?: "사용자 삭제에 실패했습니다.")
                )
            }
        }

        // 활성 사용자 목록 조회
        get {
            try {
                val users = userService.getActiveUsers()
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse.success(users.map { UserResponse.from(it) })
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse.error<Unit>("INTERNAL_ERROR", e.message ?: "사용자 목록 조회에 실패했습니다.")
                )
            }
        }

        // 사용자 통계
        get("/stats") {
            try {
                val stats = userService.getUserStats()
                call.respond(
                    HttpStatusCode.OK,
                    BaseResponse.success(UserStatsResponse(
                        totalUsers = stats.totalUsers,
                        activeUsers = stats.activeUsers,
                        deletedUsers = stats.deletedUsers
                    ))
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    BaseResponse.error<Unit>("INTERNAL_ERROR", e.message ?: "사용자 통계 조회에 실패했습니다.")
                )
            }
        }
    }
}
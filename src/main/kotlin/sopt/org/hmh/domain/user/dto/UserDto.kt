package sopt.org.hmh.domain.user.dto

import kotlinx.serialization.Serializable
import sopt.org.hmh.domain.user.entity.SocialPlatform
import sopt.org.hmh.domain.user.entity.User
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class UserResponse(
    val id: Long,
    val name: String?,
    val socialPlatform: SocialPlatform,
    val point: Int,
    val currentChallengeId: Long?,
    val isDeleted: Boolean
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                name = user.name,
                socialPlatform = user.socialPlatform,
                point = user.point,
                currentChallengeId = user.currentChallengeId,
                isDeleted = user.isDeleted
            )
        }
    }
}

@Serializable
data class UserCreateRequest(
    val socialPlatform: SocialPlatform,
    val socialId: String,
    val name: String?
)

@Serializable
data class UserUpdateRequest(
    val name: String?
)

@Serializable
data class UserPointUpdateRequest(
    val point: Int
)

@Serializable
data class UserStatsResponse(
    val totalUsers: Long,
    val activeUsers: Long,
    val deletedUsers: Long
)
package sopt.org.hmh.domain.auth.dto

import kotlinx.serialization.Serializable
import sopt.org.hmh.domain.user.entity.SocialPlatform
import sopt.org.hmh.domain.user.entity.User
import sopt.org.hmh.global.auth.social.SocialLoginRequest
import java.time.LocalDateTime

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
    val user: UserInfo,
    val isFirstLogin: Boolean
)

@Serializable
data class UserInfo(
    val id: Long,
    val name: String?,
    val socialPlatform: String,
    val point: Int,
    val currentChallengeId: Long?
) {
    companion object {
        fun from(user: User): UserInfo {
            return UserInfo(
                id = user.id,
                name = user.name,
                socialPlatform = user.socialPlatform.name,
                point = user.point,
                currentChallengeId = user.currentChallengeId
            )
        }
    }
}

@Serializable
data class ProfileUpdateRequest(
    val name: String? = null
)

@Serializable
data class SocialLoginRequestDto(
    val socialPlatform: String,
    val authorizationCode: String? = null,
    val accessToken: String? = null,
    val identityToken: String? = null
) {
    fun toSocialLoginRequest(): SocialLoginRequest {
        val platform = when (socialPlatform.uppercase()) {
            "KAKAO" -> sopt.org.hmh.global.auth.social.SocialPlatform.KAKAO
            "APPLE" -> sopt.org.hmh.global.auth.social.SocialPlatform.APPLE
            else -> throw IllegalArgumentException("지원하지 않는 소셜 플랫폼입니다: $socialPlatform")
        }
        
        return SocialLoginRequest(
            socialPlatform = platform,
            authorizationCode = authorizationCode,
            accessToken = accessToken,
            identityToken = identityToken
        )
    }
}

@Serializable
data class TokenReissueRequest(
    val refreshToken: String
)

@Serializable
data class AdminLoginRequest(
    val adminId: String,
    val password: String
)
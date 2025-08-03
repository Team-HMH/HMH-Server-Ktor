package sopt.org.hmh.global.auth.social

import kotlinx.serialization.Serializable

@Serializable
data class SocialUserInfo(
    val socialId: String,
    val socialPlatform: SocialPlatform,
    val name: String? = null,
    val email: String? = null
)

enum class SocialPlatform {
    KAKAO, APPLE
}

interface SocialLoginProvider {
    suspend fun getUserInfo(accessToken: String): SocialUserInfo
    suspend fun getAccessToken(authorizationCode: String): String
}

@Serializable
data class SocialLoginRequest(
    val socialPlatform: SocialPlatform,
    val authorizationCode: String? = null,
    val accessToken: String? = null,
    val identityToken: String? = null // Apple용
)

@Serializable
data class SocialTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long? = null,
    val refreshToken: String? = null
)
package sopt.org.hmh.global.auth.social.kakao

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import sopt.org.hmh.global.auth.jwt.JwtException
import sopt.org.hmh.global.auth.social.SocialLoginProvider
import sopt.org.hmh.global.auth.social.SocialPlatform
import sopt.org.hmh.global.auth.social.SocialUserInfo

@Serializable
data class KakaoTokenResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String? = null,
    val expires_in: Long? = null,
    val scope: String? = null,
    val refresh_token_expires_in: Long? = null
)

@Serializable
data class KakaoUserResponse(
    val id: Long,
    val connected_at: String? = null,
    val properties: KakaoProperties? = null,
    val kakao_account: KakaoAccount? = null
)

@Serializable
data class KakaoProperties(
    val nickname: String? = null,
    val profile_image: String? = null,
    val thumbnail_image: String? = null
)

@Serializable
data class KakaoAccount(
    val profile_nickname_needs_agreement: Boolean? = null,
    val profile_image_needs_agreement: Boolean? = null,
    val profile: KakaoProfile? = null,
    val has_email: Boolean? = null,
    val email_needs_agreement: Boolean? = null,
    val is_email_valid: Boolean? = null,
    val is_email_verified: Boolean? = null,
    val email: String? = null
)

@Serializable
data class KakaoProfile(
    val nickname: String? = null,
    val thumbnail_image_url: String? = null,
    val profile_image_url: String? = null,
    val is_default_image: Boolean? = null
)

class KakaoLoginProvider(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String? = null,
    private val redirectUri: String
) : SocialLoginProvider {

    companion object {
        private const val KAKAO_AUTH_URL = "https://kauth.kakao.com"
        private const val KAKAO_API_URL = "https://kapi.kakao.com"
    }

    override suspend fun getAccessToken(authorizationCode: String): String {
        val response: HttpResponse = httpClient.submitForm(
            url = "$KAKAO_AUTH_URL/oauth/token",
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("client_id", clientId)
                clientSecret?.let { append("client_secret", it) }
                append("redirect_uri", redirectUri)
                append("code", authorizationCode)
            }
        )

        if (response.status == HttpStatusCode.OK) {
            val tokenResponse = Json.decodeFromString<KakaoTokenResponse>(response.bodyAsText())
            return tokenResponse.access_token
        } else {
            throw JwtException.InvalidSocialAccessToken
        }
    }

    override suspend fun getUserInfo(accessToken: String): SocialUserInfo {
        val response: HttpResponse = httpClient.get("$KAKAO_API_URL/v2/user/me") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
        }

        if (response.status == HttpStatusCode.OK) {
            val userResponse = Json.decodeFromString<KakaoUserResponse>(response.bodyAsText())
            
            return SocialUserInfo(
                socialId = userResponse.id.toString(),
                socialPlatform = SocialPlatform.KAKAO,
                name = userResponse.kakao_account?.profile?.nickname 
                    ?: userResponse.properties?.nickname,
                email = userResponse.kakao_account?.email
            )
        } else {
            throw JwtException.InvalidSocialAccessToken
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val response: HttpResponse = httpClient.submitForm(
                url = "$KAKAO_AUTH_URL/oauth/token",
                formParameters = parameters {
                    append("grant_type", "refresh_token")
                    append("client_id", clientId)
                    clientSecret?.let { append("client_secret", it) }
                    append("refresh_token", refreshToken)
                }
            )

            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = Json.decodeFromString<KakaoTokenResponse>(response.bodyAsText())
                tokenResponse.access_token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun unlinkUser(accessToken: String): Boolean {
        return try {
            val response: HttpResponse = httpClient.post("$KAKAO_API_URL/v1/user/unlink") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }
}
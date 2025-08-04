package sopt.org.hmh.global.auth.social.apple

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
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
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Serializable
data class AppleTokenResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val expires_in: Long? = null,
    val refresh_token: String? = null,
    val id_token: String? = null
)

@Serializable
data class ApplePublicKeysResponse(
    val keys: List<ApplePublicKey>
)

@Serializable
data class ApplePublicKey(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String
)

@Serializable
data class AppleIdTokenPayload(
    val iss: String,
    val aud: String,
    val exp: Long,
    val iat: Long,
    val sub: String,
    val email: String? = null,
    val email_verified: String? = null,
    val is_private_email: String? = null,
    val real_user_status: Int? = null,
    val transfer_sub: String? = null
)

class AppleLoginProvider(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val teamId: String,
    private val keyId: String,
    private val privateKey: String,
    private val redirectUri: String
) : SocialLoginProvider {

    companion object {
        private const val APPLE_AUTH_URL = "https://appleid.apple.com"
        private const val APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys"
    }

    override suspend fun getAccessToken(authorizationCode: String): String {
        val clientSecret = generateClientSecret()
        
        val response: HttpResponse = httpClient.submitForm(
            url = "$APPLE_AUTH_URL/auth/token",
            formParameters = parameters {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", authorizationCode)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            }
        )

        if (response.status == HttpStatusCode.OK) {
            val tokenResponse = Json.decodeFromString<AppleTokenResponse>(response.bodyAsText())
            return tokenResponse.access_token ?: throw JwtException.InvalidSocialAccessToken
        } else {
            throw JwtException.InvalidSocialAccessToken
        }
    }

    override suspend fun getUserInfo(accessToken: String): SocialUserInfo {
        // Apple은 Identity Token에서 사용자 정보를 추출
        throw UnsupportedOperationException("Apple은 getUserInfoFromIdentityToken을 사용하세요")
    }

    suspend fun getUserInfoFromIdentityToken(identityToken: String): SocialUserInfo {
        val decodedToken = verifyAndDecodeIdentityToken(identityToken)
        
        return SocialUserInfo(
            socialId = decodedToken.sub,
            socialPlatform = SocialPlatform.APPLE,
            name = null, // Apple에서는 사용자가 선택적으로 제공
            email = decodedToken.email
        )
    }

    private suspend fun verifyAndDecodeIdentityToken(identityToken: String): AppleIdTokenPayload {
        try {
            // 1. JWT 헤더에서 kid 추출
            val jwt = JWT.decode(identityToken)
            val kid = jwt.getHeaderClaim("kid").asString()
                ?: throw JwtException.InvalidIdentityToken

            // 2. Apple 공개 키 조회
            val publicKey = getApplePublicKey(kid)

            // 3. JWT 검증
            val algorithm = Algorithm.RSA256(publicKey, null)
            val verifier = JWT.require(algorithm)
                .withIssuer("https://appleid.apple.com")
                .withAudience(clientId)
                .build()

            val verifiedJWT = verifier.verify(identityToken)

            // 4. 페이로드 추출
            return AppleIdTokenPayload(
                iss = verifiedJWT.issuer,
                aud = verifiedJWT.audience.first(),
                exp = verifiedJWT.expiresAt.time / 1000,
                iat = verifiedJWT.issuedAt.time / 1000,
                sub = verifiedJWT.subject,
                email = verifiedJWT.getClaim("email").asString(),
                email_verified = verifiedJWT.getClaim("email_verified").asString(),
                is_private_email = verifiedJWT.getClaim("is_private_email").asString(),
                real_user_status = verifiedJWT.getClaim("real_user_status").asInt(),
                transfer_sub = verifiedJWT.getClaim("transfer_sub").asString()
            )

        } catch (e: JWTVerificationException) {
            when {
                e.message?.contains("expired") == true -> throw JwtException.ExpiredIdentityToken
                e.message?.contains("claims") == true -> throw JwtException.InvalidIdentityTokenClaims
                else -> throw JwtException.InvalidIdentityToken
            }
        } catch (e: Exception) {
            throw JwtException.InvalidIdentityToken
        }
    }

    private suspend fun getApplePublicKey(kid: String): RSAPublicKey {
        val response: HttpResponse = httpClient.get(APPLE_KEYS_URL)
        
        if (response.status != HttpStatusCode.OK) {
            throw JwtException.UnableToCreateApplePublicKey
        }

        val keysResponse = Json.decodeFromString<ApplePublicKeysResponse>(response.bodyAsText())
        val key = keysResponse.keys.find { it.kid == kid }
            ?: throw JwtException.UnableToCreateApplePublicKey

        return generateRSAPublicKey(key.n, key.e)
    }

    private fun generateRSAPublicKey(nStr: String, eStr: String): RSAPublicKey {
        try {
            val n = BigInteger(1, Base64.getUrlDecoder().decode(nStr))
            val e = BigInteger(1, Base64.getUrlDecoder().decode(eStr))
            
            val keySpec = RSAPublicKeySpec(n, e)
            val keyFactory = KeyFactory.getInstance("RSA")
            
            return keyFactory.generatePublic(keySpec) as RSAPublicKey
        } catch (e: Exception) {
            throw JwtException.UnableToCreateApplePublicKey
        }
    }

    private fun generateClientSecret(): String {
        val now = Date()
        val expirationTime = Date(now.time + (6 * 60 * 60 * 1000)) // 6시간

        // 현재는 구현 안함 - 실제 프로덕션에서는 Apple ES256 키로 서명 필요
        throw NotImplementedError("Apple client secret 생성이 아직 구현되지 않았습니다")
    }

    private fun loadPrivateKey(): java.security.interfaces.ECPrivateKey {
        // 실제 구현에서는 privateKey 문자열을 ECPrivateKey로 변환
        // 이는 복잡한 과정이므로 여기서는 간단히 표현
        throw NotImplementedError("Private key 로딩 구현 필요")
    }

    suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            val clientSecret = generateClientSecret()
            
            val response: HttpResponse = httpClient.submitForm(
                url = "$APPLE_AUTH_URL/auth/token",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                }
            )

            if (response.status == HttpStatusCode.OK) {
                val tokenResponse = Json.decodeFromString<AppleTokenResponse>(response.bodyAsText())
                tokenResponse.access_token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun revokeToken(accessToken: String): Boolean {
        return try {
            val clientSecret = generateClientSecret()
            
            val response: HttpResponse = httpClient.submitForm(
                url = "$APPLE_AUTH_URL/auth/revoke",
                formParameters = parameters {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                    append("token", accessToken)
                    append("token_type_hint", "access_token")
                }
            )
            
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }
}
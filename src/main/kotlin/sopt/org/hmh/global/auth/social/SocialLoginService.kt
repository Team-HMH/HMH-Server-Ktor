package sopt.org.hmh.global.auth.social

import sopt.org.hmh.global.auth.social.apple.AppleLoginProvider
import sopt.org.hmh.global.auth.social.kakao.KakaoLoginProvider

class SocialLoginService(
    private val kakaoLoginProvider: KakaoLoginProvider,
    private val appleLoginProvider: AppleLoginProvider
) {
    
    suspend fun getUserInfo(request: SocialLoginRequest): SocialUserInfo {
        return when (request.socialPlatform) {
            SocialPlatform.KAKAO -> {
                val accessToken = request.accessToken 
                    ?: request.authorizationCode?.let { kakaoLoginProvider.getAccessToken(it) }
                    ?: throw SocialLoginException.CustomError("카카오 로그인에는 accessToken 또는 authorizationCode가 필요합니다")
                
                kakaoLoginProvider.getUserInfo(accessToken)
            }
            
            SocialPlatform.APPLE -> {
                val identityToken = request.identityToken
                    ?: throw SocialLoginException.CustomError("Apple 로그인에는 identityToken이 필요합니다")
                
                appleLoginProvider.getUserInfoFromIdentityToken(identityToken)
            }
        }
    }
    
    suspend fun getAccessToken(socialPlatform: SocialPlatform, authorizationCode: String): String {
        return when (socialPlatform) {
            SocialPlatform.KAKAO -> kakaoLoginProvider.getAccessToken(authorizationCode)
            SocialPlatform.APPLE -> appleLoginProvider.getAccessToken(authorizationCode)
        }
    }
    
    suspend fun refreshAccessToken(socialPlatform: SocialPlatform, refreshToken: String): String? {
        return when (socialPlatform) {
            SocialPlatform.KAKAO -> kakaoLoginProvider.refreshAccessToken(refreshToken)
            SocialPlatform.APPLE -> appleLoginProvider.refreshAccessToken(refreshToken)
        }
    }
    
    suspend fun unlinkUser(socialPlatform: SocialPlatform, accessToken: String): Boolean {
        return when (socialPlatform) {
            SocialPlatform.KAKAO -> kakaoLoginProvider.unlinkUser(accessToken)
            SocialPlatform.APPLE -> appleLoginProvider.revokeToken(accessToken)
        }
    }
}
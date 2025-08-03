package sopt.org.hmh.domain.auth.service

import sopt.org.hmh.domain.auth.dto.*
import sopt.org.hmh.domain.user.entity.SocialPlatform
import sopt.org.hmh.domain.user.entity.User
import sopt.org.hmh.domain.user.repository.UserRepository
import sopt.org.hmh.global.auth.jwt.TokenService
import sopt.org.hmh.global.auth.jwt.TokenResponse
import sopt.org.hmh.global.auth.jwt.ReissueResponse
import sopt.org.hmh.global.auth.social.SocialLoginService
import sopt.org.hmh.global.auth.social.SocialLoginRequest
import sopt.org.hmh.global.auth.social.SocialPlatform as SocialLoginPlatform

class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val socialLoginService: SocialLoginService
) {
    
    suspend fun socialLogin(request: SocialLoginRequest): LoginResponse {
        // 1. 소셜 플랫폼에서 사용자 정보 조회
        val socialUserInfo = socialLoginService.getUserInfo(request)
        
        // 2. 기존 사용자 조회 또는 신규 가입
        val user = userRepository.findBySocialPlatformAndSocialId(
            socialPlatform = socialUserInfo.socialPlatform.toDomainPlatform(),
            socialId = socialUserInfo.socialId
        ) ?: userRepository.create(
            socialPlatform = socialUserInfo.socialPlatform.toDomainPlatform(),
            socialId = socialUserInfo.socialId,
            name = socialUserInfo.name
        )
        
        // 3. JWT 토큰 발급
        val tokens = tokenService.generateTokens(user.id)
        
        return LoginResponse(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn,
            refreshTokenExpiresIn = tokens.refreshTokenExpiresIn,
            user = UserInfo.from(user),
            isFirstLogin = user.createdAt == user.updatedAt
        )
    }
    
    suspend fun reissueToken(refreshToken: String): ReissueResponse {
        return tokenService.reissueAccessToken(refreshToken)
            ?: throw AuthException.RefreshTokenExpired
    }
    
    suspend fun logout(userId: Long): Boolean {
        // 실제로는 RefreshToken을 Redis나 DB에서 관리하여 삭제
        // 현재 구현에서는 단순히 성공 응답
        return true
    }
    
    suspend fun withdrawUser(userId: Long): Boolean {
        return userRepository.softDelete(userId)
    }
    
    suspend fun getMyProfile(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            ?: throw AuthException.UserNotFound
            
        return UserInfo.from(user)
    }
    
    suspend fun updateProfile(userId: Long, request: ProfileUpdateRequest): UserInfo {
        val updatedUser = userRepository.updateUser(userId) { user ->
            user.copy(name = request.name ?: user.name)
        } ?: throw AuthException.UserNotFound
        
        return UserInfo.from(updatedUser)
    }
    
    private fun SocialLoginPlatform.toDomainPlatform(): SocialPlatform {
        return when (this) {
            SocialLoginPlatform.KAKAO -> SocialPlatform.KAKAO
            SocialLoginPlatform.APPLE -> SocialPlatform.APPLE
        }
    }
}
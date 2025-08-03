package sopt.org.hmh.global.auth.jwt

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

@Serializable 
data class ReissueResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long
)

@Serializable
data class AdminTokenResponse(
    val adminToken: String,
    val adminTokenExpiresIn: Long
)

class TokenService(private val jwtProvider: JwtProvider) {
    
    fun generateTokens(userId: Long): TokenResponse {
        val accessToken = jwtProvider.generateAccessToken(userId)
        val refreshToken = jwtProvider.generateRefreshToken(userId)
        
        return TokenResponse(
            accessToken = JwtProvider.TOKEN_PREFIX + accessToken,
            refreshToken = JwtProvider.TOKEN_PREFIX + refreshToken,
            accessTokenExpiresIn = JwtProvider.ACCESS_TOKEN_EXPIRATION_TIME,
            refreshTokenExpiresIn = JwtProvider.REFRESH_TOKEN_EXPIRATION_TIME
        )
    }
    
    fun reissueAccessToken(refreshToken: String): ReissueResponse? {
        val userId = jwtProvider.getUserIdFromToken(refreshToken) ?: return null
        
        if (jwtProvider.isTokenExpired(refreshToken)) {
            return null
        }
        
        val newAccessToken = jwtProvider.generateAccessToken(userId)
        
        return ReissueResponse(
            accessToken = JwtProvider.TOKEN_PREFIX + newAccessToken,
            accessTokenExpiresIn = JwtProvider.ACCESS_TOKEN_EXPIRATION_TIME
        )
    }
    
    fun generateAdminToken(adminId: String): AdminTokenResponse {
        val adminToken = jwtProvider.generateAdminToken(adminId)
        
        return AdminTokenResponse(
            adminToken = JwtProvider.TOKEN_PREFIX + adminToken,
            adminTokenExpiresIn = JwtProvider.ADMIN_TOKEN_EXPIRATION_TIME
        )
    }
    
    fun validateToken(token: String): Boolean {
        return jwtProvider.verifyToken(token) != null && !jwtProvider.isTokenExpired(token)
    }
    
    fun getUserIdFromToken(token: String): Long? {
        return jwtProvider.getUserIdFromToken(token)
    }
    
    fun getRoleFromToken(token: String): String? {
        return jwtProvider.getRoleFromToken(token)
    }
    
    fun getAdminIdFromToken(token: String): String? {
        return jwtProvider.getAdminIdFromToken(token)
    }
}
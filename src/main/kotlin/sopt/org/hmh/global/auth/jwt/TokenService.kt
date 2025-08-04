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
    
    fun reissueAccessToken(refreshToken: String): ReissueResponse {
        try {
            val decodedJWT = jwtProvider.verifyToken(refreshToken)
            val userId = decodedJWT.getClaim(JwtProvider.USER_CLAIM).asLong()
                ?: throw JwtException.InvalidRefreshToken
            
            if (jwtProvider.isTokenExpired(refreshToken)) {
                throw JwtException.ExpiredRefreshToken
            }
            
            val newAccessToken = jwtProvider.generateAccessToken(userId)
            
            return ReissueResponse(
                accessToken = JwtProvider.TOKEN_PREFIX + newAccessToken,
                accessTokenExpiresIn = JwtProvider.ACCESS_TOKEN_EXPIRATION_TIME
            )
        } catch (e: JwtException) {
            when (e) {
                is JwtException.ExpiredAccessToken -> throw JwtException.ExpiredRefreshToken
                is JwtException.InvalidAccessToken -> throw JwtException.InvalidRefreshToken
                else -> throw e
            }
        }
    }
    
    fun generateAdminToken(adminId: String): AdminTokenResponse {
        val adminToken = jwtProvider.generateAdminToken(adminId)
        
        return AdminTokenResponse(
            adminToken = JwtProvider.TOKEN_PREFIX + adminToken,
            adminTokenExpiresIn = JwtProvider.ADMIN_TOKEN_EXPIRATION_TIME
        )
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            jwtProvider.verifyToken(token)
            !jwtProvider.isTokenExpired(token)
        } catch (e: JwtException) {
            false
        }
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
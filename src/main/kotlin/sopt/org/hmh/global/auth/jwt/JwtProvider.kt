package sopt.org.hmh.global.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class JwtProvider(private val jwtSecret: String) {
    
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    private val issuer = "hmh-server"
    
    companion object {
        const val ACCESS_TOKEN_EXPIRATION_TIME = 2 * 60 * 60 * 1000L // 2시간
        const val REFRESH_TOKEN_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000L // 2주
        const val ADMIN_TOKEN_EXPIRATION_TIME = 6 * 60 * 60 * 1000L // 6시간
        
        const val USER_CLAIM = "userId"
        const val ROLE_CLAIM = "role"
        const val TOKEN_PREFIX = "Bearer "
    }
    
    fun generateAccessToken(userId: Long): String {
        val now = Date()
        val expiresAt = Date(now.time + ACCESS_TOKEN_EXPIRATION_TIME)
        
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(USER_CLAIM, userId)
            .withClaim(ROLE_CLAIM, "USER")
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiresAt = Date(now.time + REFRESH_TOKEN_EXPIRATION_TIME)
        
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(USER_CLAIM, userId)
            .withClaim(ROLE_CLAIM, "USER")
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    fun generateAdminToken(adminId: String): String {
        val now = Date()
        val expiresAt = Date(now.time + ADMIN_TOKEN_EXPIRATION_TIME)
        
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("adminId", adminId)
            .withClaim(ROLE_CLAIM, "ADMIN")
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }
    
    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            
            verifier.verify(extractToken(token))
        } catch (e: JWTVerificationException) {
            null
        }
    }
    
    fun getUserIdFromToken(token: String): Long? {
        val decodedJWT = verifyToken(token) ?: return null
        return decodedJWT.getClaim(USER_CLAIM).asLong()
    }
    
    fun getRoleFromToken(token: String): String? {
        val decodedJWT = verifyToken(token) ?: return null
        return decodedJWT.getClaim(ROLE_CLAIM).asString()
    }
    
    fun getAdminIdFromToken(token: String): String? {
        val decodedJWT = verifyToken(token) ?: return null
        return decodedJWT.getClaim("adminId").asString()
    }
    
    fun isTokenExpired(token: String): Boolean {
        val decodedJWT = verifyToken(token) ?: return true
        return decodedJWT.expiresAt.before(Date())
    }
    
    fun getExpirationTime(token: String): LocalDateTime? {
        val decodedJWT = verifyToken(token) ?: return null
        return decodedJWT.expiresAt.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
    
    private fun extractToken(bearerToken: String): String {
        return if (bearerToken.startsWith(TOKEN_PREFIX)) {
            bearerToken.substring(TOKEN_PREFIX.length)
        } else {
            bearerToken
        }
    }
}
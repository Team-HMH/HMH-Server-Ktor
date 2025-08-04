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
    
    fun verifyToken(token: String): DecodedJWT {
        try {
            val cleanToken = extractToken(token)
            if (cleanToken.isBlank()) {
                throw JwtException.EmptyPrincipleException
            }
            
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
            
            return verifier.verify(cleanToken)
        } catch (e: JWTVerificationException) {
            when {
                e.message?.contains("expired") == true -> throw JwtException.ExpiredAccessToken
                e.message?.contains("invalid signature") == true -> throw JwtException.InvalidAccessToken
                e.message?.contains("malformed") == true -> throw JwtException.InvalidTokenHeader
                else -> throw JwtException.InvalidAccessToken
            }
        } catch (e: JwtException) {
            throw e
        } catch (e: Exception) {
            throw JwtException.InvalidAccessToken
        }
    }
    
    fun getUserIdFromToken(token: String): Long? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.getClaim(USER_CLAIM).asLong()
        } catch (e: JwtException) {
            null
        }
    }
    
    fun getRoleFromToken(token: String): String? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.getClaim(ROLE_CLAIM).asString()
        } catch (e: JwtException) {
            null
        }
    }
    
    fun getAdminIdFromToken(token: String): String? {
        return try {
            val decodedJWT = verifyToken(token)
            val role = decodedJWT.getClaim(ROLE_CLAIM).asString()
            if (role != "ADMIN") {
                throw JwtException.InvalidAdminToken
            }
            decodedJWT.getClaim("adminId").asString()
        } catch (e: JwtException) {
            null
        }
    }
    
    fun isTokenExpired(token: String): Boolean {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.expiresAt.before(Date())
        } catch (e: JwtException) {
            true
        }
    }
    
    fun getExpirationTime(token: String): LocalDateTime? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.expiresAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (e: JwtException) {
            null
        }
    }
    
    private fun extractToken(bearerToken: String): String {
        if (bearerToken.isBlank()) {
            throw JwtException.EmptyPrincipleException
        }
        
        return if (bearerToken.startsWith(TOKEN_PREFIX)) {
            val token = bearerToken.substring(TOKEN_PREFIX.length)
            if (token.isBlank()) {
                throw JwtException.InvalidTokenHeader
            }
            token
        } else {
            bearerToken
        }
    }
}
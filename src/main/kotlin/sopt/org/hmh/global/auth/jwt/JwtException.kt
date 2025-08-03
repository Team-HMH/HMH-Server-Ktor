package sopt.org.hmh.global.auth.jwt

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class JwtException(
    message: String,
    errorCode: String,
    status: HttpStatusCode = HttpStatusCode.Unauthorized
) : BusinessException(message, errorCode, status) {
    
    object InvalidToken : JwtException(
        message = "유효하지 않은 토큰입니다.",
        errorCode = "JWT_INVALID_TOKEN"
    )
    
    object ExpiredToken : JwtException(
        message = "만료된 토큰입니다.",
        errorCode = "JWT_EXPIRED_TOKEN"
    )
    
    object MalformedToken : JwtException(
        message = "잘못된 형식의 토큰입니다.",
        errorCode = "JWT_MALFORMED_TOKEN"
    )
    
    object UnsupportedToken : JwtException(
        message = "지원하지 않는 토큰입니다.",
        errorCode = "JWT_UNSUPPORTED_TOKEN"
    )
    
    object EmptyToken : JwtException(
        message = "토큰이 비어있습니다.",
        errorCode = "JWT_EMPTY_TOKEN"
    )
    
    object InvalidSignature : JwtException(
        message = "토큰 서명이 유효하지 않습니다.",
        errorCode = "JWT_INVALID_SIGNATURE"
    )
    
    object InvalidPrefix : JwtException(
        message = "토큰 접두사가 올바르지 않습니다.",
        errorCode = "JWT_INVALID_PREFIX"
    )
    
    object RefreshTokenExpired : JwtException(
        message = "리프레시 토큰이 만료되었습니다.",
        errorCode = "JWT_REFRESH_TOKEN_EXPIRED"
    )
    
    object AdminTokenRequired : JwtException(
        message = "관리자 토큰이 필요합니다.",
        errorCode = "JWT_ADMIN_TOKEN_REQUIRED",
        status = HttpStatusCode.Forbidden
    )
}
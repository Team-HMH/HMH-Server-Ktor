package sopt.org.hmh.global.auth.jwt

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class JwtException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {
    
    data object InvalidToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_INVALID_TOKEN",
        message = "유효하지 않은 토큰입니다."
    ) {
        private fun readResolve(): Any = InvalidToken
    }

    data object ExpiredToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_EXPIRED_TOKEN",
        message = "만료된 토큰입니다."
    ) {
        private fun readResolve(): Any = ExpiredToken
    }

    data object MalformedToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_MALFORMED_TOKEN",
        message = "잘못된 형식의 토큰입니다."
    ) {
        private fun readResolve(): Any = MalformedToken
    }

    data object UnsupportedToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_UNSUPPORTED_TOKEN",
        message = "지원하지 않는 토큰입니다."
    ) {
        private fun readResolve(): Any = UnsupportedToken
    }

    data object EmptyToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_EMPTY_TOKEN",
        message = "토큰이 비어있습니다."
    ) {
        private fun readResolve(): Any = EmptyToken
    }

    data object InvalidSignature : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_INVALID_SIGNATURE",
        message = "토큰 서명이 유효하지 않습니다."
    ) {
        private fun readResolve(): Any = InvalidSignature
    }

    data object InvalidPrefix : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_INVALID_PREFIX",
        message = "토큰 접두사가 올바르지 않습니다."
    ) {
        private fun readResolve(): Any = InvalidPrefix
    }

    data object RefreshTokenExpired : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "JWT_REFRESH_TOKEN_EXPIRED",
        message = "리프레시 토큰이 만료되었습니다."
    ) {
        private fun readResolve(): Any = RefreshTokenExpired
    }

    data object AdminTokenRequired : JwtException(
        status = HttpStatusCode.Forbidden,
        errorCode = "JWT_ADMIN_TOKEN_REQUIRED",
        message = "관리자 토큰이 필요합니다."
    ) {
        private fun readResolve(): Any = AdminTokenRequired
    }
}
package sopt.org.hmh.domain.auth.service

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class AuthException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {
    
    object UserNotFound : AuthException(
        status = HttpStatusCode.NotFound,
        errorCode = "AUTH_USER_NOT_FOUND",
        message = "사용자를 찾을 수 없습니다."
    )
    
    object RefreshTokenExpired : AuthException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "AUTH_REFRESH_TOKEN_EXPIRED",
        message = "리프레시 토큰이 만료되었습니다."
    )
    
    object InvalidCredentials : AuthException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "AUTH_INVALID_CREDENTIALS",
        message = "인증 정보가 올바르지 않습니다."
    )
    
    object AccountDisabled : AuthException(
        status = HttpStatusCode.Forbidden,
        errorCode = "AUTH_ACCOUNT_DISABLED",
        message = "비활성화된 계정입니다."
    )
    
    object AccountDeleted : AuthException(
        status = HttpStatusCode.Gone,
        errorCode = "AUTH_ACCOUNT_DELETED",
        message = "삭제된 계정입니다."
    )
    
    object DuplicateUser : AuthException(
        status = HttpStatusCode.Conflict,
        errorCode = "AUTH_DUPLICATE_USER",
        message = "이미 가입된 사용자입니다."
    )
    
    object AdminNotFound : AuthException(
        status = HttpStatusCode.NotFound,
        errorCode = "AUTH_ADMIN_NOT_FOUND",
        message = "관리자를 찾을 수 없습니다."
    )
    
    object AdminInvalidCredentials : AuthException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "AUTH_ADMIN_INVALID_CREDENTIALS",
        message = "관리자 인증 정보가 올바르지 않습니다."
    )
    
    class CustomError(message: String, errorCode: String = "AUTH_ERROR") : AuthException(
        status = HttpStatusCode.BadRequest,
        errorCode = errorCode,
        message = message
    )
}
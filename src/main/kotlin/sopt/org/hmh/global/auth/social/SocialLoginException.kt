package sopt.org.hmh.global.auth.social

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class SocialLoginException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {
    
    object InvalidRequest : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_INVALID_REQUEST",
        message = "소셜 로그인 요청이 올바르지 않습니다."
    )
    
    object InvalidAuthorizationCode : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_INVALID_AUTHORIZATION_CODE",
        message = "인증 코드가 유효하지 않습니다."
    )
    
    object InvalidAccessToken : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_INVALID_ACCESS_TOKEN",
        message = "액세스 토큰이 유효하지 않습니다."
    )
    
    object InvalidIdentityToken : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_INVALID_IDENTITY_TOKEN",
        message = "Identity 토큰이 유효하지 않습니다."
    )
    
    object UserInfoRetrievalFailed : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_USER_INFO_RETRIEVAL_FAILED",
        message = "사용자 정보 조회에 실패했습니다."
    )
    
    object TokenExchangeFailed : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_TOKEN_EXCHANGE_FAILED",
        message = "토큰 교환에 실패했습니다."
    )
    
    object UnsupportedPlatform : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = "SOCIAL_LOGIN_UNSUPPORTED_PLATFORM",
        message = "지원하지 않는 소셜 플랫폼입니다."
    )
    
    object NetworkError : SocialLoginException(
        status = HttpStatusCode.BadGateway,
        errorCode = "SOCIAL_LOGIN_NETWORK_ERROR",
        message = "소셜 로그인 서버와의 통신에 실패했습니다."
    )
    
    class CustomError(message: String, errorCode: String = "SOCIAL_LOGIN_ERROR") : SocialLoginException(
        status = HttpStatusCode.BadRequest,
        errorCode = errorCode,
        message = message
    )
}
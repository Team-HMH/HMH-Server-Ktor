package sopt.org.hmh.global.auth.jwt

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class JwtException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {
    
    // 400 BAD REQUEST
    data object EmptyPrincipleException : JwtException(
        status = HttpStatusCode.BadRequest,
        errorCode = "EMPTY_PRINCIPLE_EXCEPTION",
        message = "Principle 객체가 없습니다."
    ) {
        private fun readResolve(): Any = EmptyPrincipleException
    }

    data object InvalidTokenHeader : JwtException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INVALID_TOKEN_HEADER",
        message = "토큰 헤더 값의 형식이 잘못되었습니다."
    ) {
        private fun readResolve(): Any = InvalidTokenHeader
    }

    // 401 UNAUTHORIZED - Access Token
    data object InvalidAccessToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_ACCESS_TOKEN",
        message = "유효하지 않은 액세스 토큰입니다. 액세스 토큰을 재발급 받아주세요."
    ) {
        private fun readResolve(): Any = InvalidAccessToken
    }

    data object ExpiredAccessToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "EXPIRED_ACCESS_TOKEN",
        message = "액세스 토큰이 만료되었습니다. 액세스 토큰을 재발급 받아주세요."
    ) {
        private fun readResolve(): Any = ExpiredAccessToken
    }

    // 401 UNAUTHORIZED - Refresh Token
    data object InvalidRefreshToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_REFRESH_TOKEN",
        message = "유효하지 않은 리프레시 토큰입니다. 다시 로그인 해주세요."
    ) {
        private fun readResolve(): Any = InvalidRefreshToken
    }

    data object ExpiredRefreshToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "EXPIRED_REFRESH_TOKEN",
        message = "리프레시 토큰이 만료되었습니다. 다시 로그인해 주세요."
    ) {
        private fun readResolve(): Any = ExpiredRefreshToken
    }

    // 401 UNAUTHORIZED - Social Token
    data object InvalidSocialAccessToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_SOCIAL_ACCESS_TOKEN",
        message = "유효하지 않은 소셜 엑세스 토큰입니다."
    ) {
        private fun readResolve(): Any = InvalidSocialAccessToken
    }

    data object InvalidSocialAccessTokenFormat : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_SOCIAL_ACCESS_TOKEN_FORMAT",
        message = "유효하지 않은 소셜 엑세스 토큰 형식입니다."
    ) {
        private fun readResolve(): Any = InvalidSocialAccessTokenFormat
    }

    // 401 UNAUTHORIZED - Apple Identity Token
    data object InvalidIdentityToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_IDENTITY_TOKEN",
        message = "애플 아이덴티티 토큰의 형식이 올바르지 않습니다."
    ) {
        private fun readResolve(): Any = InvalidIdentityToken
    }

    data object ExpiredIdentityToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "EXPIRED_IDENTITY_TOKEN",
        message = "만료된 애플 아이덴티티 토큰입니다."
    ) {
        private fun readResolve(): Any = ExpiredIdentityToken
    }

    data object InvalidIdentityTokenClaims : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_IDENTITY_TOKEN_CLAIMS",
        message = "유효하지 않은 애플 아이덴티티 토큰 클레임입니다."
    ) {
        private fun readResolve(): Any = InvalidIdentityTokenClaims
    }

    data object UnableToCreateApplePublicKey : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "UNABLE_TO_CREATE_APPLE_PUBLIC_KEY",
        message = "애플 로그인 중 퍼블릭 키 생성에 문제가 발생했습니다."
    ) {
        private fun readResolve(): Any = UnableToCreateApplePublicKey
    }

    // 401 UNAUTHORIZED - Admin Token
    data object InvalidAdminToken : JwtException(
        status = HttpStatusCode.Unauthorized,
        errorCode = "INVALID_ADMIN_TOKEN",
        message = "유효하지 않은 관리자 액세스 토큰입니다."
    ) {
        private fun readResolve(): Any = InvalidAdminToken
    }

    // 404 NOT FOUND
    data object NotFoundRefreshTokenError : JwtException(
        status = HttpStatusCode.NotFound,
        errorCode = "NOT_FOUND_REFRESH_TOKEN_ERROR",
        message = "존재하지 않는 리프레시 토큰입니다."
    ) {
        private fun readResolve(): Any = NotFoundRefreshTokenError
    }

    // 500 INTERNAL SERVER ERROR
    data object InternalServerError : JwtException(
        status = HttpStatusCode.InternalServerError,
        errorCode = "INTERNAL_SERVER_ERROR",
        message = "서버 내부 오류입니다."
    ) {
        private fun readResolve(): Any = InternalServerError
    }

    // 기존 호환성을 위한 별칭들
    companion object {
        @Deprecated("Use InvalidAccessToken instead", ReplaceWith("InvalidAccessToken"))
        val InvalidToken = InvalidAccessToken
        
        @Deprecated("Use ExpiredAccessToken instead", ReplaceWith("ExpiredAccessToken"))
        val ExpiredToken = ExpiredAccessToken
        
        @Deprecated("Use InvalidTokenHeader instead", ReplaceWith("InvalidTokenHeader"))
        val MalformedToken = InvalidTokenHeader
        
        @Deprecated("Use InvalidAccessToken instead", ReplaceWith("InvalidAccessToken"))
        val UnsupportedToken = InvalidAccessToken
        
        @Deprecated("Use EmptyPrincipleException instead", ReplaceWith("EmptyPrincipleException"))
        val EmptyToken = EmptyPrincipleException
        
        @Deprecated("Use InvalidAccessToken instead", ReplaceWith("InvalidAccessToken"))
        val InvalidSignature = InvalidAccessToken
        
        @Deprecated("Use InvalidTokenHeader instead", ReplaceWith("InvalidTokenHeader"))
        val InvalidPrefix = InvalidTokenHeader
        
        @Deprecated("Use ExpiredRefreshToken instead", ReplaceWith("ExpiredRefreshToken"))
        val RefreshTokenExpired = ExpiredRefreshToken
        
        @Deprecated("Use InvalidAdminToken instead", ReplaceWith("InvalidAdminToken"))
        val AdminTokenRequired = InvalidAdminToken
    }
}
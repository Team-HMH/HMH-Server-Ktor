package sopt.org.hmh.global.common.exception

import io.ktor.http.*

open class BusinessException(
    val status: HttpStatusCode,
    val errorCode: String,
    override val message: String
) : Exception(message)

class UnauthorizedException(message: String = "인증이 필요합니다.") : 
    BusinessException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", message)

class ForbiddenException(message: String = "권한이 부족합니다.") : 
    BusinessException(HttpStatusCode.Forbidden, "FORBIDDEN", message)

class NotFoundException(message: String = "리소스를 찾을 수 없습니다.") : 
    BusinessException(HttpStatusCode.NotFound, "NOT_FOUND", message)

class BadRequestException(message: String = "잘못된 요청입니다.") : 
    BusinessException(HttpStatusCode.BadRequest, "BAD_REQUEST", message)

class ConflictException(message: String = "충돌이 발생했습니다.") : 
    BusinessException(HttpStatusCode.Conflict, "CONFLICT", message)
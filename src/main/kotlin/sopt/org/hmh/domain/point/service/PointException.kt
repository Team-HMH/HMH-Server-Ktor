package sopt.org.hmh.domain.point.service

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class PointException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {

    data object InsufficientPoints : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INSUFFICIENT_POINTS",
        message = "포인트가 부족합니다."
    ) {
        private fun readResolve(): Any = InsufficientPoints
    }

    data object InvalidPointAmount : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INVALID_POINT_AMOUNT",
        message = "유효하지 않은 포인트 수량입니다."
    ) {
        private fun readResolve(): Any = InvalidPointAmount
    }

    data object PointOperationFailed : PointException(
        status = HttpStatusCode.InternalServerError,
        errorCode = "POINT_OPERATION_FAILED",
        message = "포인트 처리에 실패했습니다."
    ) {
        private fun readResolve(): Any = PointOperationFailed
    }

    data object AlreadyEarnedPoint : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "ALREADY_EARNED_POINT",
        message = "이미 포인트를 획득했습니다."
    ) {
        private fun readResolve(): Any = AlreadyEarnedPoint
    }

    data object AlreadyUsedPoint : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "ALREADY_USED_POINT",
        message = "이미 포인트를 사용했습니다."
    ) {
        private fun readResolve(): Any = AlreadyUsedPoint
    }

    data object CannotEarnPoint : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "CANNOT_EARN_POINT",
        message = "포인트를 획득할 수 없습니다."
    ) {
        private fun readResolve(): Any = CannotEarnPoint
    }

    data object CannotUsePoint : PointException(
        status = HttpStatusCode.BadRequest,
        errorCode = "CANNOT_USE_POINT",
        message = "포인트를 사용할 수 없습니다."
    ) {
        private fun readResolve(): Any = CannotUsePoint
    }
}
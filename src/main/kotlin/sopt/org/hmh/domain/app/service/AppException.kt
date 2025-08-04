package sopt.org.hmh.domain.app.service

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class AppException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {

    data object ChallengeAppNotFound : AppException(
        status = HttpStatusCode.NotFound,
        errorCode = "CHALLENGE_APP_NOT_FOUND",
        message = "챌린지 앱을 찾을 수 없습니다."
    ) {
        private fun readResolve(): Any = ChallengeAppNotFound
    }

    data object ChallengeAppAlreadyExists : AppException(
        status = HttpStatusCode.Conflict,
        errorCode = "CHALLENGE_APP_ALREADY_EXISTS",
        message = "이미 등록된 챌린지 앱입니다."
    ) {
        private fun readResolve(): Any = ChallengeAppAlreadyExists
    }

    data object HistoryAppNotFound : AppException(
        status = HttpStatusCode.NotFound,
        errorCode = "HISTORY_APP_NOT_FOUND",
        message = "앱 사용 기록을 찾을 수 없습니다."
    ) {
        private fun readResolve(): Any = HistoryAppNotFound
    }

    data object HistoryAppAlreadyExists : AppException(
        status = HttpStatusCode.Conflict,
        errorCode = "HISTORY_APP_ALREADY_EXISTS",
        message = "이미 등록된 앱 사용 기록입니다."
    ) {
        private fun readResolve(): Any = HistoryAppAlreadyExists
    }

    data object InvalidAppCode : AppException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INVALID_APP_CODE",
        message = "유효하지 않은 앱 코드입니다."
    ) {
        private fun readResolve(): Any = InvalidAppCode
    }

    data object InvalidGoalTime : AppException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INVALID_GOAL_TIME",
        message = "유효하지 않은 목표 시간입니다."
    ) {
        private fun readResolve(): Any = InvalidGoalTime
    }

    data object InvalidUsageTime : AppException(
        status = HttpStatusCode.BadRequest,
        errorCode = "INVALID_USAGE_TIME",
        message = "유효하지 않은 사용 시간입니다."
    ) {
        private fun readResolve(): Any = InvalidUsageTime
    }
}
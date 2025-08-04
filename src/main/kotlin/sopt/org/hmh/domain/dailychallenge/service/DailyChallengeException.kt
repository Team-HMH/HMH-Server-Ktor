package sopt.org.hmh.domain.dailychallenge.service

import io.ktor.http.*
import sopt.org.hmh.global.common.exception.BusinessException

sealed class DailyChallengeException(
    status: HttpStatusCode,
    errorCode: String,
    message: String
) : BusinessException(status, errorCode, message) {

    data object DailyChallengeNotFound : DailyChallengeException(
        status = HttpStatusCode.NotFound,
        errorCode = "DAILY_CHALLENGE_NOT_FOUND",
        message = "일일 챌린지를 찾을 수 없습니다."
    ) {
        private fun readResolve(): Any = DailyChallengeNotFound
    }

    data object DailyChallengeAlreadyExists : DailyChallengeException(
        status = HttpStatusCode.Conflict,
        errorCode = "DAILY_CHALLENGE_ALREADY_EXISTS",
        message = "이미 등록된 일일 챌린지입니다."
    ) {
        private fun readResolve(): Any = DailyChallengeAlreadyExists
    }

    data object DailyChallengeAlreadyProcessed : DailyChallengeException(
        status = HttpStatusCode.BadRequest,
        errorCode = "DAILY_CHALLENGE_ALREADY_PROCESSED",
        message = "이미 처리된 일일 챌린지입니다."
    ) {
        private fun readResolve(): Any = DailyChallengeAlreadyProcessed
    }

    data object PeriodIndexNotValid : DailyChallengeException(
        status = HttpStatusCode.BadRequest,
        errorCode = "PERIOD_INDEX_NOT_VALID",
        message = "유효하지 않은 기간 인덱스입니다."
    ) {
        private fun readResolve(): Any = PeriodIndexNotValid
    }

    data object DailyChallengePeriodIndexNotFound : DailyChallengeException(
        status = HttpStatusCode.NotFound,
        errorCode = "DAILY_CHALLENGE_PERIOD_INDEX_NOT_FOUND",
        message = "해당 기간 인덱스의 일일 챌린지를 찾을 수 없습니다."
    ) {
        private fun readResolve(): Any = DailyChallengePeriodIndexNotFound
    }

    data object InvalidStatus : DailyChallengeException(
        status = HttpStatusCode.BadRequest,
        errorCode = "DAILY_CHALLENGE_INVALID_STATUS",
        message = "유효하지 않은 상태입니다."
    ) {
        private fun readResolve(): Any = InvalidStatus
    }
}
package sopt.org.hmh.domain.dailychallenge.dto

import kotlinx.serialization.Serializable
import sopt.org.hmh.domain.dailychallenge.entity.Status
import java.time.LocalDate

@Serializable
data class FinishedDailyChallengeRequest(
    val challengeId: Long,
    val currentDate: String // "2024-01-01" format
)

@Serializable
data class FinishedDailyChallengeListRequest(
    val challengeIdList: List<Long>,
    val currentDate: String // "2024-01-01" format
)

@Serializable
data class FinishedDailyChallengeStatusRequest(
    val challengeId: Long,
    val status: String // "EARNED" or "UNEARNED"
)

@Serializable
data class FinishedDailyChallengeStatusListRequest(
    val challengeIdList: List<Long>,
    val status: String // "EARNED" or "UNEARNED"
)

@Serializable
data class DailyChallengeResponse(
    val id: Long,
    val challengeId: Long,
    val userId: Long,
    val status: String,
    val goalTime: Long,
    val challengeDate: String,
    val isFinished: Boolean
) {
    companion object {
        fun from(dailyChallenge: sopt.org.hmh.domain.dailychallenge.entity.DailyChallenge): DailyChallengeResponse {
            return DailyChallengeResponse(
                id = dailyChallenge.id,
                challengeId = dailyChallenge.challengeId,
                userId = dailyChallenge.userId,
                status = dailyChallenge.status.name,
                goalTime = dailyChallenge.goalTime,
                challengeDate = dailyChallenge.challengeDate.toString(),
                isFinished = dailyChallenge.status != Status.NONE
            )
        }
    }
}

@Serializable
data class ChallengeStatusResponse(
    val challengeId: Long,
    val status: String,
    val isFinished: Boolean
)

@Serializable
data class ChallengeStatusesResponse(
    val challengeStatuses: List<ChallengeStatusResponse>
)
package sopt.org.hmh.domain.challenge.dto

import kotlinx.serialization.Serializable
import sopt.org.hmh.domain.challenge.entity.Challenge
import java.time.LocalDate

@Serializable
data class ChallengeResponse(
    val id: Long,
    val userId: Long,
    val period: Int,
    val goalTime: Long,
    val startDate: String // LocalDate를 String으로 직렬화
) {
    companion object {
        fun from(challenge: Challenge): ChallengeResponse {
            return ChallengeResponse(
                id = challenge.id,
                userId = challenge.userId,
                period = challenge.period,
                goalTime = challenge.goalTime,
                startDate = challenge.startDate.toString()
            )
        }
    }
}

@Serializable
data class ChallengeCreateRequest(
    val period: Int,
    val goalTime: Long,
    val startDate: String // LocalDate를 String으로 받음
) {
    fun toLocalDate(): LocalDate = LocalDate.parse(startDate)
}

@Serializable
data class ChallengeUpdateRequest(
    val period: Int? = null,
    val goalTime: Long? = null,
    val startDate: String? = null
) {
    fun toLocalDate(): LocalDate? = startDate?.let { LocalDate.parse(it) }
}

@Serializable
data class ChallengeStatsResponse(
    val totalChallenges: Long,
    val hasCurrentChallenge: Boolean,
    val currentChallengeId: Long?
)
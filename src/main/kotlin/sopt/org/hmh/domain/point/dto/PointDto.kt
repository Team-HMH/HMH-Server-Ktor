package sopt.org.hmh.domain.point.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChallengePointStatusResponse(
    val challengeId: Long,
    val status: String,
    val earnedPoint: Int,
    val canEarnPoint: Boolean,
    val canUsePoint: Boolean
)

@Serializable
data class ChallengePointStatusListResponse(
    val challengePointStatuses: List<ChallengePointStatusResponse>
)

@Serializable
data class EarnPointResponse(
    val earnedPoint: Int,
    val totalPoint: Int,
    val message: String = "포인트를 획득했습니다!"
)

@Serializable
data class EarnedPointResponse(
    val earnedPoint: Int
)

@Serializable
data class UsePointResponse(
    val usedPoint: Int,
    val totalPoint: Int,
    val message: String = "포인트를 사용했습니다!"
)

@Serializable
data class UsagePointResponse(
    val usagePoint: Int
)

@Serializable
data class ChallengeDateRequest(
    val challengeDate: String // "2024-01-01" format
)
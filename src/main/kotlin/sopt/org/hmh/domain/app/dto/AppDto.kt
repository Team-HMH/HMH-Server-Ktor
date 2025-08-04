package sopt.org.hmh.domain.app.dto

import kotlinx.serialization.Serializable
import sopt.org.hmh.domain.app.entity.ChallengeApp
import sopt.org.hmh.domain.app.entity.HistoryApp

@Serializable
data class ChallengeAppRequest(
    val challengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val os: String
)

@Serializable
data class ChallengeAppArrayRequest(
    val challengeId: Long,
    val apps: List<AppInfo>
)

@Serializable
data class AppInfo(
    val appCode: String,
    val goalTime: Long
)

@Serializable
data class HistoryAppRequest(
    val dailyChallengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val usageTime: Long,
    val os: String
)

@Serializable
data class AppRemoveRequest(
    val challengeId: Long,
    val appCode: String
)

@Serializable
data class ChallengeAppResponse(
    val id: Long,
    val challengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val os: String
) {
    companion object {
        fun from(challengeApp: ChallengeApp): ChallengeAppResponse {
            return ChallengeAppResponse(
                id = challengeApp.id,
                challengeId = challengeApp.challengeId,
                appCode = challengeApp.appCode,
                goalTime = challengeApp.goalTime,
                os = challengeApp.os
            )
        }
    }
}

@Serializable
data class HistoryAppResponse(
    val id: Long,
    val dailyChallengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val usageTime: Long,
    val os: String,
    val achievementRate: Double // 달성률 (usageTime / goalTime * 100)
) {
    companion object {
        fun from(historyApp: HistoryApp): HistoryAppResponse {
            val achievementRate = if (historyApp.goalTime > 0) {
                (historyApp.usageTime.toDouble() / historyApp.goalTime.toDouble() * 100)
            } else 0.0
            
            return HistoryAppResponse(
                id = historyApp.id,
                dailyChallengeId = historyApp.dailyChallengeId,
                appCode = historyApp.appCode,
                goalTime = historyApp.goalTime,
                usageTime = historyApp.usageTime,
                os = historyApp.os,
                achievementRate = achievementRate
            )
        }
    }
}
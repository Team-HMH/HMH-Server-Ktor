package sopt.org.hmh.domain.app.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenges
import java.time.LocalDateTime

object HistoryApps : LongIdTable("history_apps") {
    val dailyChallengeId = long("daily_challenge_id").references(DailyChallenges.id)
    val appCode = varchar("app_code", 255)
    val goalTime = long("goal_time")
    val usageTime = long("usage_time")
    val os = varchar("os", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

data class HistoryApp(
    val id: Long,
    val dailyChallengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val usageTime: Long,
    val os: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun ResultRow.toHistoryApp(): HistoryApp = HistoryApp(
    id = this[HistoryApps.id].value,
    dailyChallengeId = this[HistoryApps.dailyChallengeId],
    appCode = this[HistoryApps.appCode],
    goalTime = this[HistoryApps.goalTime],
    usageTime = this[HistoryApps.usageTime],
    os = this[HistoryApps.os],
    createdAt = this[HistoryApps.createdAt],
    updatedAt = this[HistoryApps.updatedAt]
)
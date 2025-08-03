package sopt.org.hmh.domain.app.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import sopt.org.hmh.domain.challenge.entity.Challenges
import java.time.LocalDateTime

object ChallengeApps : LongIdTable("challenge_apps") {
    val challengeId = long("challenge_id").references(Challenges.id)
    val appCode = varchar("app_code", 255)
    val goalTime = long("goal_time")
    val os = varchar("os", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

data class ChallengeApp(
    val id: Long,
    val challengeId: Long,
    val appCode: String,
    val goalTime: Long,
    val os: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun ResultRow.toChallengeApp(): ChallengeApp = ChallengeApp(
    id = this[ChallengeApps.id].value,
    challengeId = this[ChallengeApps.challengeId],
    appCode = this[ChallengeApps.appCode],
    goalTime = this[ChallengeApps.goalTime],
    os = this[ChallengeApps.os],
    createdAt = this[ChallengeApps.createdAt],
    updatedAt = this[ChallengeApps.updatedAt]
)
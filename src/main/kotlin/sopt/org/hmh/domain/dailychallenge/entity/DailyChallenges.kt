package sopt.org.hmh.domain.dailychallenge.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import sopt.org.hmh.domain.challenge.entity.Challenges
import sopt.org.hmh.domain.user.entity.Users
import java.time.LocalDate
import java.time.LocalDateTime

object DailyChallenges : LongIdTable("daily_challenges") {
    val challengeId = long("challenge_id").references(Challenges.id)
    val userId = long("user_id").references(Users.id)
    val status = enumerationByName("status", 10, Status::class).default(Status.NONE)
    val goalTime = long("goal_time")
    val challengeDate = date("challenge_date")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

enum class Status {
    NONE, FAILURE, EARNED, UNEARNED
}

data class DailyChallenge(
    val id: Long,
    val challengeId: Long,
    val userId: Long,
    val status: Status,
    val goalTime: Long,
    val challengeDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun ResultRow.toDailyChallenge(): DailyChallenge = DailyChallenge(
    id = this[DailyChallenges.id].value,
    challengeId = this[DailyChallenges.challengeId],
    userId = this[DailyChallenges.userId],
    status = this[DailyChallenges.status],
    goalTime = this[DailyChallenges.goalTime],
    challengeDate = this[DailyChallenges.challengeDate],
    createdAt = this[DailyChallenges.createdAt],
    updatedAt = this[DailyChallenges.updatedAt]
)
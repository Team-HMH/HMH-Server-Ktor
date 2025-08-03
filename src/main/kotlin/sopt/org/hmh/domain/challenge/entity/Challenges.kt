package sopt.org.hmh.domain.challenge.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import sopt.org.hmh.domain.user.entity.Users
import java.time.LocalDate
import java.time.LocalDateTime

object Challenges : LongIdTable("challenges") {
    val userId = long("user_id").references(Users.id)
    val period = integer("period")
    val goalTime = long("goal_time")
    val startDate = date("start_date")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

data class Challenge(
    val id: Long,
    val userId: Long,
    val period: Int,
    val goalTime: Long,
    val startDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun ResultRow.toChallenge(): Challenge = Challenge(
    id = this[Challenges.id].value,
    userId = this[Challenges.userId],
    period = this[Challenges.period],
    goalTime = this[Challenges.goalTime],
    startDate = this[Challenges.startDate],
    createdAt = this[Challenges.createdAt],
    updatedAt = this[Challenges.updatedAt]
)
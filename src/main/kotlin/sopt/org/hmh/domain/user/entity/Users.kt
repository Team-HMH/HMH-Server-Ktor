package sopt.org.hmh.domain.user.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime

object Users : LongIdTable("users") {
    val name = varchar("name", 255).nullable()
    val currentChallengeId = long("current_challenge_id").nullable()
    val socialPlatform = enumerationByName("social_platform", 10, SocialPlatform::class)
    val socialId = varchar("social_id", 255).uniqueIndex()
    val point = integer("point").default(0)
    val recentLockDate = date("recent_lock_date").nullable()
    val isDeleted = bool("is_deleted").default(false)
    val deletedAt = datetime("deleted_at").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

enum class SocialPlatform {
    KAKAO, APPLE
}

data class User(
    val id: Long,
    val name: String?,
    val currentChallengeId: Long?,
    val socialPlatform: SocialPlatform,
    val socialId: String,
    val point: Int,
    val recentLockDate: LocalDate?,
    val isDeleted: Boolean,
    val deletedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        const val INITIAL_POINT = 0
        const val MEMBER_INFO_RETENTION_PERIOD = 30L // days
    }
    
    fun decreasePoint(usagePoint: Int): Int {
        require(point >= usagePoint) { "포인트가 부족합니다." }
        return point - usagePoint
    }
    
    fun increasePoint(earnedPoint: Int): Int = point + earnedPoint
}

fun ResultRow.toUser(): User = User(
    id = this[Users.id].value,
    name = this[Users.name],
    currentChallengeId = this[Users.currentChallengeId],
    socialPlatform = this[Users.socialPlatform],
    socialId = this[Users.socialId],
    point = this[Users.point],
    recentLockDate = this[Users.recentLockDate],
    isDeleted = this[Users.isDeleted],
    deletedAt = this[Users.deletedAt],
    createdAt = this[Users.createdAt],
    updatedAt = this[Users.updatedAt]
)
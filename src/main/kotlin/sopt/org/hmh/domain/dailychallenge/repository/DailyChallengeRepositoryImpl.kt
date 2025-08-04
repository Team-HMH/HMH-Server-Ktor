package sopt.org.hmh.domain.dailychallenge.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenge
import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenges
import sopt.org.hmh.domain.dailychallenge.entity.Status
import sopt.org.hmh.domain.dailychallenge.entity.toDailyChallenge
import sopt.org.hmh.global.common.database.dbQuery
import java.time.LocalDate
import java.time.LocalDateTime

class DailyChallengeRepositoryImpl : DailyChallengeRepository {

    override suspend fun saveAll(dailyChallenges: List<DailyChallenge>): List<DailyChallenge> = dbQuery {
        val insertedRows = DailyChallenges.batchInsert(dailyChallenges) { dailyChallenge ->
            this[DailyChallenges.challengeId] = dailyChallenge.challengeId
            this[DailyChallenges.userId] = dailyChallenge.userId
            this[DailyChallenges.status] = dailyChallenge.status
            this[DailyChallenges.goalTime] = dailyChallenge.goalTime
            this[DailyChallenges.challengeDate] = dailyChallenge.challengeDate
            this[DailyChallenges.createdAt] = LocalDateTime.now()
            this[DailyChallenges.updatedAt] = LocalDateTime.now()
        }
        
        insertedRows.map { it.toDailyChallenge() }
    }

    override suspend fun save(dailyChallenge: DailyChallenge): DailyChallenge = dbQuery {
        val insertedId = DailyChallenges.insertAndGetId {
            it[challengeId] = dailyChallenge.challengeId
            it[userId] = dailyChallenge.userId
            it[status] = dailyChallenge.status
            it[goalTime] = dailyChallenge.goalTime
            it[challengeDate] = dailyChallenge.challengeDate
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        DailyChallenges.select { DailyChallenges.id eq insertedId }
            .single()
            .toDailyChallenge()
    }

    override suspend fun findByChallengeDateAndUserId(challengeDate: LocalDate, userId: Long): DailyChallenge? = dbQuery {
        DailyChallenges.select { (DailyChallenges.challengeDate eq challengeDate) and (DailyChallenges.userId eq userId) }
            .singleOrNull()
            ?.toDailyChallenge()
    }

    override suspend fun findAllByChallengeId(challengeId: Long): List<DailyChallenge> = dbQuery {
        DailyChallenges.select { DailyChallenges.challengeId eq challengeId }
            .map { it.toDailyChallenge() }
    }

    override suspend fun findAllByUserIdAndChallengeId(userId: Long, challengeId: Long): List<DailyChallenge> = dbQuery {
        DailyChallenges.select { (DailyChallenges.userId eq userId) and (DailyChallenges.challengeId eq challengeId) }
            .map { it.toDailyChallenge() }
    }

    override suspend fun findAllByUserIdAndChallengeIdIn(userId: Long, challengeIds: List<Long>): List<DailyChallenge> = dbQuery {
        DailyChallenges.select { (DailyChallenges.userId eq userId) and (DailyChallenges.challengeId inList challengeIds) }
            .map { it.toDailyChallenge() }
    }

    override suspend fun findAllByUserIdAndChallengeDateBetween(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyChallenge> = dbQuery {
        DailyChallenges.select { 
                (DailyChallenges.userId eq userId) and 
                (DailyChallenges.challengeDate greaterEq startDate) and 
                (DailyChallenges.challengeDate lessEq endDate) 
            }
            .map { it.toDailyChallenge() }
    }

    override suspend fun existsByUserIdAndChallengeDateIn(userId: Long, challengeDates: List<LocalDate>): Boolean = dbQuery {
        DailyChallenges.select { (DailyChallenges.userId eq userId) and (DailyChallenges.challengeDate inList challengeDates) }
            .count() > 0
    }

    override suspend fun updateStatus(id: Long, status: Status): Boolean = dbQuery {
        DailyChallenges.update({ DailyChallenges.id eq id }) {
            it[DailyChallenges.status] = status
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    override suspend fun updateStatusByChallengeIds(userId: Long, challengeIds: List<Long>, status: Status): Int = dbQuery {
        DailyChallenges.update({ 
            (DailyChallenges.userId eq userId) and (DailyChallenges.challengeId inList challengeIds) 
        }) {
            it[DailyChallenges.status] = status
            it[updatedAt] = LocalDateTime.now()
        }
    }

    override suspend fun deleteById(id: Long): Boolean = dbQuery {
        DailyChallenges.deleteWhere { DailyChallenges.id eq id } > 0
    }

    override suspend fun deleteAllByChallengeId(challengeId: Long): Int = dbQuery {
        DailyChallenges.deleteWhere { DailyChallenges.challengeId eq challengeId }
    }
}
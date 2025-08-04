package sopt.org.hmh.domain.challenge.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import sopt.org.hmh.domain.challenge.entity.*
import sopt.org.hmh.global.common.database.dbQuery
import java.time.LocalDate
import java.time.LocalDateTime

class ChallengeRepositoryImpl : ChallengeRepository {

    override suspend fun findById(id: Long): Challenge? = dbQuery {
        Challenges.select { Challenges.id eq id }
            .singleOrNull()
            ?.toChallenge()
    }

    override suspend fun findByUserId(userId: Long): List<Challenge> = dbQuery {
        Challenges.select { Challenges.userId eq userId }
            .orderBy(Challenges.createdAt, SortOrder.DESC)
            .map { it.toChallenge() }
    }

    override suspend fun findCurrentChallengeByUserId(userId: Long): Challenge? = dbQuery {
        Challenges.select { Challenges.userId eq userId }
            .orderBy(Challenges.createdAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toChallenge()
    }

    override suspend fun create(
        userId: Long, 
        period: Int, 
        goalTime: Long, 
        startDate: LocalDate
    ): Challenge = dbQuery {
        val challengeId = Challenges.insertAndGetId {
            it[Challenges.userId] = userId
            it[Challenges.period] = period
            it[Challenges.goalTime] = goalTime
            it[Challenges.startDate] = startDate
        }
        
        Challenges.select { Challenges.id eq challengeId }
            .single()
            .toChallenge()
    }

    override suspend fun update(id: Long, updater: (Challenge) -> Challenge): Challenge? = dbQuery {
        val currentChallenge = Challenges.select { Challenges.id eq id }
            .singleOrNull()
            ?.toChallenge() ?: return@dbQuery null
            
        val updatedChallenge = updater(currentChallenge)
        
        Challenges.update({ Challenges.id eq id }) {
            it[period] = updatedChallenge.period
            it[goalTime] = updatedChallenge.goalTime
            it[startDate] = updatedChallenge.startDate
            it[updatedAt] = LocalDateTime.now()
        }
        
        Challenges.select { Challenges.id eq id }
            .singleOrNull()
            ?.toChallenge()
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        val deletedRows = Challenges.deleteWhere { Challenges.id eq id }
        deletedRows > 0
    }

    override suspend fun findChallengesByDateRange(
        userId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<Challenge> = dbQuery {
        Challenges.select { 
            (Challenges.userId eq userId) and 
            (Challenges.startDate greaterEq startDate) and 
            (Challenges.startDate lessEq endDate) 
        }
            .orderBy(Challenges.startDate, SortOrder.ASC)
            .map { it.toChallenge() }
    }

    override suspend fun countChallengesByUserId(userId: Long): Long = dbQuery {
        Challenges.select { Challenges.userId eq userId }
            .count()
    }
}
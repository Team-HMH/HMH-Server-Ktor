package sopt.org.hmh.domain.challenge.repository

import sopt.org.hmh.domain.challenge.entity.Challenge
import java.time.LocalDate

interface ChallengeRepository {
    suspend fun findById(id: Long): Challenge?
    suspend fun findByUserId(userId: Long): List<Challenge>
    suspend fun findCurrentChallengeByUserId(userId: Long): Challenge?
    suspend fun create(userId: Long, period: Int, goalTime: Long, startDate: LocalDate): Challenge
    suspend fun update(id: Long, updater: (Challenge) -> Challenge): Challenge?
    suspend fun delete(id: Long): Boolean
    suspend fun findChallengesByDateRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<Challenge>
    suspend fun countChallengesByUserId(userId: Long): Long
}
package sopt.org.hmh.domain.dailychallenge.repository

import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenge
import sopt.org.hmh.domain.dailychallenge.entity.Status
import java.time.LocalDate

interface DailyChallengeRepository {
    suspend fun saveAll(dailyChallenges: List<DailyChallenge>): List<DailyChallenge>
    suspend fun save(dailyChallenge: DailyChallenge): DailyChallenge
    suspend fun findByChallengeDateAndUserId(challengeDate: LocalDate, userId: Long): DailyChallenge?
    suspend fun findAllByChallengeId(challengeId: Long): List<DailyChallenge>
    suspend fun findAllByUserIdAndChallengeId(userId: Long, challengeId: Long): List<DailyChallenge>
    suspend fun findAllByUserIdAndChallengeIdIn(userId: Long, challengeIds: List<Long>): List<DailyChallenge>
    suspend fun findAllByUserIdAndChallengeDateBetween(
        userId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<DailyChallenge>
    suspend fun existsByUserIdAndChallengeDateIn(userId: Long, challengeDates: List<LocalDate>): Boolean
    suspend fun updateStatus(id: Long, status: Status): Boolean
    suspend fun updateStatusByChallengeIds(userId: Long, challengeIds: List<Long>, status: Status): Int
    suspend fun deleteById(id: Long): Boolean
    suspend fun deleteAllByChallengeId(challengeId: Long): Int
}
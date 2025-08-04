package sopt.org.hmh.domain.dailychallenge.service

import sopt.org.hmh.domain.challenge.entity.Challenge
import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenge
import sopt.org.hmh.domain.dailychallenge.entity.Status
import sopt.org.hmh.domain.dailychallenge.repository.DailyChallengeRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class DailyChallengeService(
    private val dailyChallengeRepository: DailyChallengeRepository
) {

    suspend fun findDailyChallengeByChallengeDateAndUserId(challengeDate: LocalDate, userId: Long): DailyChallenge {
        return dailyChallengeRepository.findByChallengeDateAndUserId(challengeDate, userId)
            ?: throw DailyChallengeException.DailyChallengeNotFound
    }

    suspend fun findDailyChallengesByUserIdAndChallengeId(userId: Long, challengeId: Long): List<DailyChallenge> {
        return dailyChallengeRepository.findAllByUserIdAndChallengeId(userId, challengeId)
    }

    suspend fun findDailyChallengesByUserIdAndChallengeIds(userId: Long, challengeIds: List<Long>): List<DailyChallenge> {
        return dailyChallengeRepository.findAllByUserIdAndChallengeIdIn(userId, challengeIds)
    }

    suspend fun findDailyChallengesByChallengeDateRange(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailyChallenge> {
        return dailyChallengeRepository.findAllByUserIdAndChallengeDateBetween(userId, startDate, endDate)
    }

    fun validateDailyChallengeStatus(dailyChallengeStatus: Status, expectedStatuses: List<Status>) {
        if (!expectedStatuses.contains(dailyChallengeStatus)) {
            throw DailyChallengeException.DailyChallengeAlreadyProcessed
        }
    }

    suspend fun changeStatusByCurrentStatus(dailyChallenge: DailyChallenge): DailyChallenge {
        val newStatus = when (dailyChallenge.status) {
            Status.NONE -> Status.UNEARNED
            Status.FAILURE -> return dailyChallenge // 실패 상태는 그대로 유지
            else -> throw DailyChallengeException.DailyChallengeAlreadyProcessed
        }
        
        dailyChallengeRepository.updateStatus(dailyChallenge.id, newStatus)
        return dailyChallenge.copy(status = newStatus)
    }

    suspend fun addDailyChallenge(challenge: Challenge): List<DailyChallenge> {
        validateDuplicateDailyChallenge(challenge)
        val dailyChallenges = createDailyChallengeByChallengePeriod(challenge)
        return dailyChallengeRepository.saveAll(dailyChallenges)
    }

    private suspend fun validateDuplicateDailyChallenge(challenge: Challenge) {
        val challengeDates = (0 until challenge.period).map { 
            challenge.startDate.plusDays(it.toLong()) 
        }
        
        if (dailyChallengeRepository.existsByUserIdAndChallengeDateIn(challenge.userId, challengeDates)) {
            throw DailyChallengeException.DailyChallengeAlreadyExists
        }
    }

    fun validatePeriodIndex(periodIndex: Int, todayIndex: Int) {
        if (periodIndex >= todayIndex) {
            throw DailyChallengeException.PeriodIndexNotValid
        }
    }

    private fun createDailyChallengeByChallengePeriod(challenge: Challenge): List<DailyChallenge> {
        return (0 until challenge.period).map { i ->
            DailyChallenge(
                id = 0, // 임시 ID, 저장 시 실제 ID 부여
                challengeId = challenge.id,
                userId = challenge.userId,
                status = Status.NONE,
                goalTime = challenge.goalTime,
                challengeDate = challenge.startDate.plusDays(i.toLong()),
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
        }
    }

    suspend fun getDailyChallengesByChallengeId(challengeId: Long): List<DailyChallenge> {
        return dailyChallengeRepository.findAllByChallengeId(challengeId)
    }

    suspend fun updateDailyChallengeStatus(userId: Long, challengeId: Long, status: Status): Boolean {
        val statusName = when (status) {
            Status.EARNED, Status.UNEARNED -> status
            else -> throw DailyChallengeException.InvalidStatus
        }
        
        return dailyChallengeRepository.updateStatusByChallengeIds(userId, listOf(challengeId), statusName) > 0
    }

    suspend fun updateDailyChallengeStatusList(userId: Long, challengeIds: List<Long>, status: Status): Int {
        val statusName = when (status) {
            Status.EARNED, Status.UNEARNED -> status
            else -> throw DailyChallengeException.InvalidStatus
        }
        
        return dailyChallengeRepository.updateStatusByChallengeIds(userId, challengeIds, statusName)
    }

    suspend fun finishDailyChallenge(userId: Long, challengeId: Long, currentDate: LocalDate): DailyChallenge {
        val dailyChallenge = findDailyChallengeByChallengeDateAndUserId(currentDate, userId)
        
        if (dailyChallenge.challengeId != challengeId) {
            throw DailyChallengeException.DailyChallengeNotFound
        }
        
        return changeStatusByCurrentStatus(dailyChallenge)
    }

    suspend fun finishDailyChallengeList(userId: Long, challengeIds: List<Long>, currentDate: LocalDate): List<DailyChallenge> {
        return challengeIds.mapNotNull { challengeId ->
            try {
                finishDailyChallenge(userId, challengeId, currentDate)
            } catch (e: DailyChallengeException) {
                null // 실패한 경우 무시
            }
        }
    }

    fun calculateTodayIndex(challenge: Challenge, now: LocalDate): Int {
        val completedChallengeIndex = -1
        val daysBetween = ChronoUnit.DAYS.between(challenge.startDate, now).toInt()
        return if (daysBetween >= challenge.period) completedChallengeIndex else max(0, daysBetween)
    }

    fun parseStatus(statusString: String): Status {
        return try {
            Status.valueOf(statusString.uppercase())
        } catch (e: IllegalArgumentException) {
            throw DailyChallengeException.InvalidStatus
        }
    }
}
package sopt.org.hmh.domain.app.service

import sopt.org.hmh.domain.app.entity.HistoryApp
import sopt.org.hmh.domain.app.repository.HistoryAppRepository
import java.time.LocalDateTime

class HistoryAppService(
    private val historyAppRepository: HistoryAppRepository
) {

    suspend fun addHistoryApp(
        dailyChallengeId: Long, 
        appCode: String, 
        goalTime: Long, 
        usageTime: Long, 
        os: String
    ): HistoryApp {
        validateAppCode(appCode)
        validateGoalTime(goalTime)
        validateUsageTime(usageTime)
        
        if (historyAppRepository.existsByDailyChallengeIdAndAppCode(dailyChallengeId, appCode)) {
            throw AppException.HistoryAppAlreadyExists
        }
        
        val historyApp = HistoryApp(
            id = 0, // 임시 ID, 저장 시 실제 ID 부여
            dailyChallengeId = dailyChallengeId,
            appCode = appCode,
            goalTime = goalTime,
            usageTime = usageTime,
            os = os,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return historyAppRepository.save(historyApp)
    }

    suspend fun addHistoryApps(historyApps: List<HistoryApp>): List<HistoryApp> {
        historyApps.forEach { historyApp ->
            validateAppCode(historyApp.appCode)
            validateGoalTime(historyApp.goalTime)
            validateUsageTime(historyApp.usageTime)
            
            if (historyAppRepository.existsByDailyChallengeIdAndAppCode(historyApp.dailyChallengeId, historyApp.appCode)) {
                throw AppException.HistoryAppAlreadyExists
            }
        }
        
        return historyAppRepository.saveAll(historyApps)
    }

    suspend fun getHistoryAppsByDailyChallengeId(dailyChallengeId: Long): List<HistoryApp> {
        return historyAppRepository.findAllByDailyChallengeId(dailyChallengeId)
    }

    suspend fun getHistoryAppsByDailyChallengeIds(dailyChallengeIds: List<Long>): List<HistoryApp> {
        return historyAppRepository.findAllByDailyChallengeIdIn(dailyChallengeIds)
    }

    suspend fun getHistoryApp(dailyChallengeId: Long, appCode: String): HistoryApp {
        return historyAppRepository.findByDailyChallengeIdAndAppCode(dailyChallengeId, appCode)
            ?: throw AppException.HistoryAppNotFound
    }

    suspend fun updateHistoryAppUsageTime(id: Long, usageTime: Long): Boolean {
        validateUsageTime(usageTime)
        return historyAppRepository.updateUsageTime(id, usageTime)
    }

    suspend fun removeHistoryApp(dailyChallengeId: Long, appCode: String): Boolean {
        if (!historyAppRepository.existsByDailyChallengeIdAndAppCode(dailyChallengeId, appCode)) {
            throw AppException.HistoryAppNotFound
        }
        
        return historyAppRepository.deleteByDailyChallengeIdAndAppCode(dailyChallengeId, appCode)
    }

    suspend fun removeAllHistoryApps(dailyChallengeId: Long): Int {
        return historyAppRepository.deleteAllByDailyChallengeId(dailyChallengeId)
    }

    fun calculateAchievementRate(goalTime: Long, usageTime: Long): Double {
        return if (goalTime > 0) {
            (usageTime.toDouble() / goalTime.toDouble() * 100)
        } else 0.0
    }

    private fun validateAppCode(appCode: String) {
        if (appCode.isBlank()) {
            throw AppException.InvalidAppCode
        }
    }

    private fun validateGoalTime(goalTime: Long) {
        if (goalTime <= 0) {
            throw AppException.InvalidGoalTime
        }
    }

    private fun validateUsageTime(usageTime: Long) {
        if (usageTime < 0) {
            throw AppException.InvalidUsageTime
        }
    }
}
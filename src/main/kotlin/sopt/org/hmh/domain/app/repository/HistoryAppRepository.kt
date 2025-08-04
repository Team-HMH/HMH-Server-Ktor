package sopt.org.hmh.domain.app.repository

import sopt.org.hmh.domain.app.entity.HistoryApp

interface HistoryAppRepository {
    suspend fun save(historyApp: HistoryApp): HistoryApp
    suspend fun saveAll(historyApps: List<HistoryApp>): List<HistoryApp>
    suspend fun findById(id: Long): HistoryApp?
    suspend fun findAllByDailyChallengeId(dailyChallengeId: Long): List<HistoryApp>
    suspend fun findAllByDailyChallengeIdIn(dailyChallengeIds: List<Long>): List<HistoryApp>
    suspend fun findByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): HistoryApp?
    suspend fun existsByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): Boolean
    suspend fun deleteById(id: Long): Boolean
    suspend fun deleteByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): Boolean
    suspend fun deleteAllByDailyChallengeId(dailyChallengeId: Long): Int
    suspend fun updateUsageTime(id: Long, usageTime: Long): Boolean
}
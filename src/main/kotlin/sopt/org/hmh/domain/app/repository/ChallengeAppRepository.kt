package sopt.org.hmh.domain.app.repository

import sopt.org.hmh.domain.app.entity.ChallengeApp

interface ChallengeAppRepository {
    suspend fun save(challengeApp: ChallengeApp): ChallengeApp
    suspend fun saveAll(challengeApps: List<ChallengeApp>): List<ChallengeApp>
    suspend fun findById(id: Long): ChallengeApp?
    suspend fun findAllByChallengeId(challengeId: Long): List<ChallengeApp>
    suspend fun findAllByChallengeIdIn(challengeIds: List<Long>): List<ChallengeApp>
    suspend fun findByChallengeIdAndAppCode(challengeId: Long, appCode: String): ChallengeApp?
    suspend fun existsByChallengeIdAndAppCode(challengeId: Long, appCode: String): Boolean
    suspend fun deleteById(id: Long): Boolean
    suspend fun deleteByChallengeIdAndAppCode(challengeId: Long, appCode: String): Boolean
    suspend fun deleteAllByChallengeId(challengeId: Long): Int
    suspend fun updateGoalTime(id: Long, goalTime: Long): Boolean
}
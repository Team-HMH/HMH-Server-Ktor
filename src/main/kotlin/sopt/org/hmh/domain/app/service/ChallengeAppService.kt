package sopt.org.hmh.domain.app.service

import sopt.org.hmh.domain.app.dto.AppInfo
import sopt.org.hmh.domain.app.entity.ChallengeApp
import sopt.org.hmh.domain.app.repository.ChallengeAppRepository
import java.time.LocalDateTime

class ChallengeAppService(
    private val challengeAppRepository: ChallengeAppRepository
) {

    suspend fun addChallengeApp(challengeId: Long, appCode: String, goalTime: Long, os: String): ChallengeApp {
        validateAppCode(appCode)
        validateGoalTime(goalTime)
        
        if (challengeAppRepository.existsByChallengeIdAndAppCode(challengeId, appCode)) {
            throw AppException.ChallengeAppAlreadyExists
        }
        
        val challengeApp = ChallengeApp(
            id = 0, // 임시 ID, 저장 시 실제 ID 부여
            challengeId = challengeId,
            appCode = appCode,
            goalTime = goalTime,
            os = os,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return challengeAppRepository.save(challengeApp)
    }

    suspend fun addChallengeApps(challengeId: Long, apps: List<AppInfo>, os: String): List<ChallengeApp> {
        apps.forEach { app ->
            validateAppCode(app.appCode)
            validateGoalTime(app.goalTime)
            
            if (challengeAppRepository.existsByChallengeIdAndAppCode(challengeId, app.appCode)) {
                throw AppException.ChallengeAppAlreadyExists
            }
        }
        
        val challengeApps = apps.map { app ->
            ChallengeApp(
                id = 0, // 임시 ID, 저장 시 실제 ID 부여
                challengeId = challengeId,
                appCode = app.appCode,
                goalTime = app.goalTime,
                os = os,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
        
        return challengeAppRepository.saveAll(challengeApps)
    }

    suspend fun getChallengeAppsByChallengeId(challengeId: Long): List<ChallengeApp> {
        return challengeAppRepository.findAllByChallengeId(challengeId)
    }

    suspend fun getChallengeAppsByChallengeIds(challengeIds: List<Long>): List<ChallengeApp> {
        return challengeAppRepository.findAllByChallengeIdIn(challengeIds)
    }

    suspend fun getChallengeApp(challengeId: Long, appCode: String): ChallengeApp {
        return challengeAppRepository.findByChallengeIdAndAppCode(challengeId, appCode)
            ?: throw AppException.ChallengeAppNotFound
    }

    suspend fun updateChallengeAppGoalTime(id: Long, goalTime: Long): Boolean {
        validateGoalTime(goalTime)
        return challengeAppRepository.updateGoalTime(id, goalTime)
    }

    suspend fun removeChallengeApp(challengeId: Long, appCode: String): Boolean {
        if (!challengeAppRepository.existsByChallengeIdAndAppCode(challengeId, appCode)) {
            throw AppException.ChallengeAppNotFound
        }
        
        return challengeAppRepository.deleteByChallengeIdAndAppCode(challengeId, appCode)
    }

    suspend fun removeAllChallengeApps(challengeId: Long): Int {
        return challengeAppRepository.deleteAllByChallengeId(challengeId)
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
}
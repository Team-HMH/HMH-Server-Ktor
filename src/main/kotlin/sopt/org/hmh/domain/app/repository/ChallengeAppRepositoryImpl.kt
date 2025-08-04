package sopt.org.hmh.domain.app.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import sopt.org.hmh.domain.app.entity.ChallengeApp
import sopt.org.hmh.domain.app.entity.ChallengeApps
import sopt.org.hmh.domain.app.entity.toChallengeApp
import sopt.org.hmh.global.common.database.dbQuery
import java.time.LocalDateTime

class ChallengeAppRepositoryImpl : ChallengeAppRepository {

    override suspend fun save(challengeApp: ChallengeApp): ChallengeApp = dbQuery {
        val insertedId = ChallengeApps.insertAndGetId {
            it[challengeId] = challengeApp.challengeId
            it[appCode] = challengeApp.appCode
            it[goalTime] = challengeApp.goalTime
            it[os] = challengeApp.os
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        ChallengeApps.select { ChallengeApps.id eq insertedId }
            .single()
            .toChallengeApp()
    }

    override suspend fun saveAll(challengeApps: List<ChallengeApp>): List<ChallengeApp> = dbQuery {
        val insertedRows = ChallengeApps.batchInsert(challengeApps) { challengeApp ->
            this[ChallengeApps.challengeId] = challengeApp.challengeId
            this[ChallengeApps.appCode] = challengeApp.appCode
            this[ChallengeApps.goalTime] = challengeApp.goalTime
            this[ChallengeApps.os] = challengeApp.os
            this[ChallengeApps.createdAt] = LocalDateTime.now()
            this[ChallengeApps.updatedAt] = LocalDateTime.now()
        }
        
        insertedRows.map { it.toChallengeApp() }
    }

    override suspend fun findById(id: Long): ChallengeApp? = dbQuery {
        ChallengeApps.select { ChallengeApps.id eq id }
            .singleOrNull()
            ?.toChallengeApp()
    }

    override suspend fun findAllByChallengeId(challengeId: Long): List<ChallengeApp> = dbQuery {
        ChallengeApps.select { ChallengeApps.challengeId eq challengeId }
            .map { it.toChallengeApp() }
    }

    override suspend fun findAllByChallengeIdIn(challengeIds: List<Long>): List<ChallengeApp> = dbQuery {
        ChallengeApps.select { ChallengeApps.challengeId inList challengeIds }
            .map { it.toChallengeApp() }
    }

    override suspend fun findByChallengeIdAndAppCode(challengeId: Long, appCode: String): ChallengeApp? = dbQuery {
        ChallengeApps.select { (ChallengeApps.challengeId eq challengeId) and (ChallengeApps.appCode eq appCode) }
            .singleOrNull()
            ?.toChallengeApp()
    }

    override suspend fun existsByChallengeIdAndAppCode(challengeId: Long, appCode: String): Boolean = dbQuery {
        ChallengeApps.select { (ChallengeApps.challengeId eq challengeId) and (ChallengeApps.appCode eq appCode) }
            .count() > 0
    }

    override suspend fun deleteById(id: Long): Boolean = dbQuery {
        ChallengeApps.deleteWhere { ChallengeApps.id eq id } > 0
    }

    override suspend fun deleteByChallengeIdAndAppCode(challengeId: Long, appCode: String): Boolean = dbQuery {
        ChallengeApps.deleteWhere { 
            (ChallengeApps.challengeId eq challengeId) and (ChallengeApps.appCode eq appCode) 
        } > 0
    }

    override suspend fun deleteAllByChallengeId(challengeId: Long): Int = dbQuery {
        ChallengeApps.deleteWhere { ChallengeApps.challengeId eq challengeId }
    }

    override suspend fun updateGoalTime(id: Long, goalTime: Long): Boolean = dbQuery {
        ChallengeApps.update({ ChallengeApps.id eq id }) {
            it[ChallengeApps.goalTime] = goalTime
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }
}
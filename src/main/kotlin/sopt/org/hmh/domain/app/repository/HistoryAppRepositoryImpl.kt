package sopt.org.hmh.domain.app.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import sopt.org.hmh.domain.app.entity.HistoryApp
import sopt.org.hmh.domain.app.entity.HistoryApps
import sopt.org.hmh.domain.app.entity.toHistoryApp
import sopt.org.hmh.global.common.database.dbQuery
import java.time.LocalDateTime

class HistoryAppRepositoryImpl : HistoryAppRepository {

    override suspend fun save(historyApp: HistoryApp): HistoryApp = dbQuery {
        val insertedId = HistoryApps.insertAndGetId {
            it[dailyChallengeId] = historyApp.dailyChallengeId
            it[appCode] = historyApp.appCode
            it[goalTime] = historyApp.goalTime
            it[usageTime] = historyApp.usageTime
            it[os] = historyApp.os
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        
        HistoryApps.select { HistoryApps.id eq insertedId }
            .single()
            .toHistoryApp()
    }

    override suspend fun saveAll(historyApps: List<HistoryApp>): List<HistoryApp> = dbQuery {
        val insertedRows = HistoryApps.batchInsert(historyApps) { historyApp ->
            this[HistoryApps.dailyChallengeId] = historyApp.dailyChallengeId
            this[HistoryApps.appCode] = historyApp.appCode
            this[HistoryApps.goalTime] = historyApp.goalTime
            this[HistoryApps.usageTime] = historyApp.usageTime
            this[HistoryApps.os] = historyApp.os
            this[HistoryApps.createdAt] = LocalDateTime.now()
            this[HistoryApps.updatedAt] = LocalDateTime.now()
        }
        
        insertedRows.map { it.toHistoryApp() }
    }

    override suspend fun findById(id: Long): HistoryApp? = dbQuery {
        HistoryApps.select { HistoryApps.id eq id }
            .singleOrNull()
            ?.toHistoryApp()
    }

    override suspend fun findAllByDailyChallengeId(dailyChallengeId: Long): List<HistoryApp> = dbQuery {
        HistoryApps.select { HistoryApps.dailyChallengeId eq dailyChallengeId }
            .map { it.toHistoryApp() }
    }

    override suspend fun findAllByDailyChallengeIdIn(dailyChallengeIds: List<Long>): List<HistoryApp> = dbQuery {
        HistoryApps.select { HistoryApps.dailyChallengeId inList dailyChallengeIds }
            .map { it.toHistoryApp() }
    }

    override suspend fun findByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): HistoryApp? = dbQuery {
        HistoryApps.select { (HistoryApps.dailyChallengeId eq dailyChallengeId) and (HistoryApps.appCode eq appCode) }
            .singleOrNull()
            ?.toHistoryApp()
    }

    override suspend fun existsByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): Boolean = dbQuery {
        HistoryApps.select { (HistoryApps.dailyChallengeId eq dailyChallengeId) and (HistoryApps.appCode eq appCode) }
            .count() > 0
    }

    override suspend fun deleteById(id: Long): Boolean = dbQuery {
        HistoryApps.deleteWhere { HistoryApps.id eq id } > 0
    }

    override suspend fun deleteByDailyChallengeIdAndAppCode(dailyChallengeId: Long, appCode: String): Boolean = dbQuery {
        HistoryApps.deleteWhere { 
            (HistoryApps.dailyChallengeId eq dailyChallengeId) and (HistoryApps.appCode eq appCode) 
        } > 0
    }

    override suspend fun deleteAllByDailyChallengeId(dailyChallengeId: Long): Int = dbQuery {
        HistoryApps.deleteWhere { HistoryApps.dailyChallengeId eq dailyChallengeId }
    }

    override suspend fun updateUsageTime(id: Long, usageTime: Long): Boolean = dbQuery {
        HistoryApps.update({ HistoryApps.id eq id }) {
            it[HistoryApps.usageTime] = usageTime
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }
}
package sopt.org.hmh.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import sopt.org.hmh.domain.user.entity.Users
import sopt.org.hmh.domain.challenge.entity.Challenges
import sopt.org.hmh.domain.dailychallenge.entity.DailyChallenges
import sopt.org.hmh.domain.app.entity.ChallengeApps

fun Application.configureDatabase() {
    try {
        val config = environment.config
        
        // 데이터베이스 설정이 있는 경우에만 연결 시도
        val dbUrl = config.propertyOrNull("database.url")?.getString()
        if (dbUrl.isNullOrEmpty()) {
            println("⚠️ 데이터베이스 설정이 없습니다. 데이터베이스 기능은 비활성화됩니다.")
            return
        }
        
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = dbUrl
            driverClassName = config.property("database.driver").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = config.propertyOrNull("database.maximumPoolSize")?.getString()?.toInt() ?: 20
            minimumIdle = config.propertyOrNull("database.minimumIdle")?.getString()?.toInt() ?: 5
            connectionTimeout = config.propertyOrNull("database.connectionTimeout")?.getString()?.toLong() ?: 30000
            idleTimeout = config.propertyOrNull("database.idleTimeout")?.getString()?.toLong() ?: 600000
            maxLifetime = config.propertyOrNull("database.maxLifetime")?.getString()?.toLong() ?: 1800000
            
            // MySQL 최적화 설정
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            addDataSourceProperty("useServerPrepStmts", "true")
            addDataSourceProperty("useLocalSessionState", "true")
            addDataSourceProperty("rewriteBatchedStatements", "true")
            addDataSourceProperty("cacheResultSetMetadata", "true")
            addDataSourceProperty("cacheServerConfiguration", "true")
            addDataSourceProperty("elideSetAutoCommits", "true")
            addDataSourceProperty("maintainTimeStats", "false")
        }
        
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
        
        println("✅ 데이터베이스 연결 성공: $dbUrl")
        
        // 테이블 생성 (개발 환경에서만)
        if (config.propertyOrNull("ktor.development")?.getString()?.toBoolean() == true) {
            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    Users,
                    Challenges, 
                    DailyChallenges,
                    ChallengeApps
                )
            }
            println("✅ 데이터베이스 테이블 초기화 완료")
        }
        
    } catch (e: Exception) {
        println("⚠️ 데이터베이스 연결 실패: ${e.message}")
        println("   서버는 데이터베이스 없이 실행됩니다.")
    }
}
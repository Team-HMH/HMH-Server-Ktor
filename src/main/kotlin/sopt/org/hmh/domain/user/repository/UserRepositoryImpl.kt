package sopt.org.hmh.domain.user.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import sopt.org.hmh.domain.user.entity.*
import sopt.org.hmh.global.common.database.dbQuery
import java.time.LocalDateTime

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: Long): User? = dbQuery {
        Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findBySocialId(socialId: String): User? = dbQuery {
        Users.select { Users.socialId eq socialId }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun findBySocialPlatformAndSocialId(
        socialPlatform: SocialPlatform, 
        socialId: String
    ): User? = dbQuery {
        Users.select { (Users.socialPlatform eq socialPlatform) and (Users.socialId eq socialId) }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun save(user: User): User = dbQuery {
        val userId = Users.insertAndGetId {
            it[name] = user.name
            it[socialPlatform] = user.socialPlatform
            it[socialId] = user.socialId
            it[point] = user.point
            it[currentChallengeId] = user.currentChallengeId
            it[recentLockDate] = user.recentLockDate
            it[isDeleted] = user.isDeleted
            it[deletedAt] = user.deletedAt
        }
        
        Users.select { Users.id eq userId }
            .single()
            .toUser()
    }

    override suspend fun create(
        socialPlatform: SocialPlatform, 
        socialId: String, 
        name: String?
    ): User = dbQuery {
        val userId = Users.insertAndGetId {
            it[Users.name] = name
            it[Users.socialPlatform] = socialPlatform
            it[Users.socialId] = socialId
            it[point] = User.INITIAL_POINT
            it[isDeleted] = false
        }
        
        Users.select { Users.id eq userId }
            .single()
            .toUser()
    }

    override suspend fun updateUser(id: Long, updater: (User) -> User): User? = dbQuery {
        val currentUser = Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUser() ?: return@dbQuery null
            
        val updatedUser = updater(currentUser)
        
        Users.update({ Users.id eq id }) {
            it[name] = updatedUser.name
            it[currentChallengeId] = updatedUser.currentChallengeId
            it[point] = updatedUser.point
            it[recentLockDate] = updatedUser.recentLockDate
            it[isDeleted] = updatedUser.isDeleted
            it[deletedAt] = updatedUser.deletedAt
            it[updatedAt] = LocalDateTime.now()
        }
        
        Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUser()
    }

    override suspend fun softDelete(id: Long): Boolean = dbQuery {
        val deletionDate = LocalDateTime.now().plusDays(User.MEMBER_INFO_RETENTION_PERIOD)
        val updatedRows = Users.update({ Users.id eq id }) {
            it[isDeleted] = true
            it[point] = 0
            it[deletedAt] = deletionDate
            it[updatedAt] = LocalDateTime.now()
        }
        updatedRows > 0
    }

    override suspend fun recover(id: Long): Boolean = dbQuery {
        val updatedRows = Users.update({ Users.id eq id }) {
            it[isDeleted] = false
            it[deletedAt] = null
            it[updatedAt] = LocalDateTime.now()
        }
        updatedRows > 0
    }

    override suspend fun findActiveUsers(): List<User> = dbQuery {
        Users.select { Users.isDeleted eq false }
            .map { it.toUser() }
    }

    override suspend fun countTotalUsers(): Long = dbQuery {
        Users.selectAll().count()
    }

    override suspend fun countActiveUsers(): Long = dbQuery {
        Users.select { Users.isDeleted eq false }
            .count()
    }
}
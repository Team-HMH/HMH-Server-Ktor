package sopt.org.hmh.domain.user.repository

import sopt.org.hmh.domain.user.entity.User
import sopt.org.hmh.domain.user.entity.SocialPlatform

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findBySocialId(socialId: String): User?
    suspend fun findBySocialPlatformAndSocialId(socialPlatform: SocialPlatform, socialId: String): User?
    suspend fun save(user: User): User
    suspend fun create(socialPlatform: SocialPlatform, socialId: String, name: String?): User
    suspend fun updateUser(id: Long, updater: (User) -> User): User?
    suspend fun softDelete(id: Long): Boolean
    suspend fun recover(id: Long): Boolean
    suspend fun findActiveUsers(): List<User>
    suspend fun countTotalUsers(): Long
    suspend fun countActiveUsers(): Long
}
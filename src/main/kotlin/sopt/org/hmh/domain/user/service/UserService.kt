package sopt.org.hmh.domain.user.service

import sopt.org.hmh.domain.user.entity.SocialPlatform
import sopt.org.hmh.domain.user.entity.User
import sopt.org.hmh.domain.user.repository.UserRepository
import sopt.org.hmh.global.common.exception.NotFoundException
import sopt.org.hmh.global.common.exception.ConflictException

class UserService(
    private val userRepository: UserRepository
) {
    
    suspend fun getUserById(id: Long): User {
        return userRepository.findById(id) 
            ?: throw NotFoundException("사용자를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun getUserBySocialInfo(socialPlatform: SocialPlatform, socialId: String): User? {
        return userRepository.findBySocialPlatformAndSocialId(socialPlatform, socialId)
    }
    
    suspend fun createUser(socialPlatform: SocialPlatform, socialId: String, name: String?): User {
        // 이미 존재하는 사용자인지 확인
        val existingUser = userRepository.findBySocialPlatformAndSocialId(socialPlatform, socialId)
        if (existingUser != null) {
            throw ConflictException("이미 존재하는 사용자입니다.")
        }
        
        return userRepository.create(socialPlatform, socialId, name)
    }
    
    suspend fun updateUserName(id: Long, name: String): User {
        return userRepository.updateUser(id) { user ->
            user.copy(name = name)
        } ?: throw NotFoundException("사용자를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun updateUserPoint(id: Long, point: Int): User {
        return userRepository.updateUser(id) { user ->
            user.copy(point = point)
        } ?: throw NotFoundException("사용자를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun increaseUserPoint(id: Long, earnedPoint: Int): User {
        return userRepository.updateUser(id) { user ->
            user.copy(point = user.increasePoint(earnedPoint))
        } ?: throw NotFoundException("사용자를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun decreaseUserPoint(id: Long, usagePoint: Int): User {
        return userRepository.updateUser(id) { user ->
            user.copy(point = user.decreasePoint(usagePoint))
        } ?: throw NotFoundException("사용자를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun softDeleteUser(id: Long): Boolean {
        val user = getUserById(id) // 존재 여부 확인
        return userRepository.softDelete(id)
    }
    
    suspend fun recoverUser(id: Long): Boolean {
        return userRepository.recover(id)
    }
    
    suspend fun getActiveUsers(): List<User> {
        return userRepository.findActiveUsers()
    }
    
    suspend fun getUserStats(): UserStats {
        val totalUsers = userRepository.countTotalUsers()
        val activeUsers = userRepository.countActiveUsers()
        return UserStats(
            totalUsers = totalUsers,
            activeUsers = activeUsers,
            deletedUsers = totalUsers - activeUsers
        )
    }
}

data class UserStats(
    val totalUsers: Long,
    val activeUsers: Long,
    val deletedUsers: Long
)
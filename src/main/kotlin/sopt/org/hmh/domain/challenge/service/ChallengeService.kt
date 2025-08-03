package sopt.org.hmh.domain.challenge.service

import sopt.org.hmh.domain.challenge.entity.Challenge
import sopt.org.hmh.domain.challenge.repository.ChallengeRepository
import sopt.org.hmh.domain.user.service.UserService
import sopt.org.hmh.global.common.exception.NotFoundException
import sopt.org.hmh.global.common.exception.BadRequestException
import java.time.LocalDate

class ChallengeService(
    private val challengeRepository: ChallengeRepository,
    private val userService: UserService
) {
    
    suspend fun getChallengeById(id: Long): Challenge {
        return challengeRepository.findById(id)
            ?: throw NotFoundException("챌린지를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun getChallengesByUserId(userId: Long): List<Challenge> {
        // 사용자 존재 여부 확인
        userService.getUserById(userId)
        return challengeRepository.findByUserId(userId)
    }
    
    suspend fun getCurrentChallengeByUserId(userId: Long): Challenge? {
        // 사용자 존재 여부 확인
        userService.getUserById(userId)
        return challengeRepository.findCurrentChallengeByUserId(userId)
    }
    
    suspend fun createChallenge(
        userId: Long, 
        period: Int, 
        goalTime: Long, 
        startDate: LocalDate
    ): Challenge {
        // 사용자 존재 여부 확인
        userService.getUserById(userId)
        
        // 비즈니스 로직 검증
        validateChallengeData(period, goalTime, startDate)
        
        return challengeRepository.create(userId, period, goalTime, startDate)
    }
    
    suspend fun updateChallengeStartDate(id: Long, startDate: LocalDate): Challenge {
        validateStartDate(startDate)
        
        return challengeRepository.update(id) { challenge ->
            challenge.copy(startDate = startDate)
        } ?: throw NotFoundException("챌린지를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun updateChallengeGoalTime(id: Long, goalTime: Long): Challenge {
        validateGoalTime(goalTime)
        
        return challengeRepository.update(id) { challenge ->
            challenge.copy(goalTime = goalTime)
        } ?: throw NotFoundException("챌린지를 찾을 수 없습니다. ID: $id")
    }
    
    suspend fun deleteChallenge(id: Long): Boolean {
        val challenge = getChallengeById(id) // 존재 여부 확인
        return challengeRepository.delete(id)
    }
    
    suspend fun getChallengesByDateRange(
        userId: Long, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): List<Challenge> {
        // 사용자 존재 여부 확인
        userService.getUserById(userId)
        
        if (startDate.isAfter(endDate)) {
            throw BadRequestException("시작 날짜는 종료 날짜보다 이전이어야 합니다.")
        }
        
        return challengeRepository.findChallengesByDateRange(userId, startDate, endDate)
    }
    
    suspend fun getChallengeStats(userId: Long): ChallengeStats {
        // 사용자 존재 여부 확인
        userService.getUserById(userId)
        
        val totalChallenges = challengeRepository.countChallengesByUserId(userId)
        val currentChallenge = challengeRepository.findCurrentChallengeByUserId(userId)
        
        return ChallengeStats(
            totalChallenges = totalChallenges,
            hasCurrentChallenge = currentChallenge != null,
            currentChallengeId = currentChallenge?.id
        )
    }
    
    private fun validateChallengeData(period: Int, goalTime: Long, startDate: LocalDate) {
        validatePeriod(period)
        validateGoalTime(goalTime)
        validateStartDate(startDate)
    }
    
    private fun validatePeriod(period: Int) {
        if (period <= 0 || period > 365) {
            throw BadRequestException("챌린지 기간은 1일 이상 365일 이하여야 합니다.")
        }
    }
    
    private fun validateGoalTime(goalTime: Long) {
        if (goalTime <= 0 || goalTime > 24 * 60 * 60 * 1000) { // 24시간을 밀리초로
            throw BadRequestException("목표 시간은 0보다 크고 24시간 이하여야 합니다.")
        }
    }
    
    private fun validateStartDate(startDate: LocalDate) {
        val today = LocalDate.now()
        if (startDate.isBefore(today.minusDays(30))) {
            throw BadRequestException("시작 날짜는 30일 이전으로 설정할 수 없습니다.")
        }
        if (startDate.isAfter(today.plusDays(365))) {
            throw BadRequestException("시작 날짜는 1년 이후로 설정할 수 없습니다.")
        }
    }
}

data class ChallengeStats(
    val totalChallenges: Long,
    val hasCurrentChallenge: Boolean,
    val currentChallengeId: Long?
)
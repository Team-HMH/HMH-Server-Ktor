package sopt.org.hmh.domain.point.service

import sopt.org.hmh.domain.challenge.entity.Challenge
import sopt.org.hmh.domain.challenge.service.ChallengeService
import sopt.org.hmh.domain.dailychallenge.entity.Status
import sopt.org.hmh.domain.dailychallenge.service.DailyChallengeService
import sopt.org.hmh.domain.point.dto.*
import sopt.org.hmh.domain.user.service.UserService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PointService(
    private val userService: UserService,
    private val dailyChallengeService: DailyChallengeService,
    private val challengeService: ChallengeService
) {
    companion object {
        const val EARNED_POINT = 10 // 챌린지 성공 시 획득 포인트
        const val USAGE_POINT = 20  // 챌린지 실패 시 사용 포인트
    }

    suspend fun earnPointAndChallengeEarned(userId: Long, challengeDate: LocalDate): EarnPointResponse {
        val user = userService.findUserById(userId)
        val dailyChallenge = dailyChallengeService.findDailyChallengeByChallengeDateAndUserId(challengeDate, userId)
        
        // 이미 포인트를 획득했는지 확인
        if (dailyChallenge.status == Status.EARNED) {
            throw PointException.AlreadyEarnedPoint
        }
        
        // 포인트 획득 가능한 상태인지 확인 (UNEARNED 상태여야 함)
        if (dailyChallenge.status != Status.UNEARNED) {
            throw PointException.CannotEarnPoint
        }
        
        // 포인트 적립
        val updatedUser = userService.addPoint(userId, EARNED_POINT)
        
        // 일일 챌린지 상태를 EARNED로 변경
        dailyChallengeService.changeStatusByCurrentStatus(dailyChallenge.copy(status = Status.EARNED))
        
        return EarnPointResponse(
            earnedPoint = EARNED_POINT,
            totalPoint = updatedUser.point.toInt(),
            message = "포인트를 획득했습니다!"
        )
    }

    suspend fun usePointAndTodayDailyChallengeFailed(userId: Long): UsePointResponse {
        val user = userService.findUserById(userId)
        
        // 포인트가 충분한지 확인
        if (user.point < USAGE_POINT) {
            throw PointException.InsufficientPoints
        }
        
        // 오늘 날짜로 일일 챌린지 찾기
        val today = LocalDate.now()
        val dailyChallenge = dailyChallengeService.findDailyChallengeByChallengeDateAndUserId(today, userId)
        
        // 이미 포인트를 사용했는지 확인
        if (dailyChallenge.status == Status.FAILURE) {
            throw PointException.AlreadyUsedPoint
        }
        
        // 포인트 사용 가능한 상태인지 확인 (NONE 상태여야 함)
        if (dailyChallenge.status != Status.NONE) {
            throw PointException.CannotUsePoint
        }
        
        // 포인트 차감
        val updatedUser = userService.usePoint(userId, USAGE_POINT)
        
        // 일일 챌린지 상태를 FAILURE로 변경
        dailyChallengeService.changeStatusByCurrentStatus(dailyChallenge.copy(status = Status.FAILURE))
        
        return UsePointResponse(
            usedPoint = USAGE_POINT,
            totalPoint = updatedUser.point.toInt(),
            message = "포인트를 사용했습니다!"
        )
    }

    suspend fun usePointAndChallengeFailedDeprecated(userId: Long, challengeDate: LocalDate): UsePointResponse {
        val user = userService.findUserById(userId)
        
        // 포인트가 충분한지 확인
        if (user.point < USAGE_POINT) {
            throw PointException.InsufficientPoints
        }
        
        val dailyChallenge = dailyChallengeService.findDailyChallengeByChallengeDateAndUserId(challengeDate, userId)
        
        // 이미 포인트를 사용했는지 확인
        if (dailyChallenge.status == Status.FAILURE) {
            throw PointException.AlreadyUsedPoint
        }
        
        // 포인트 사용 가능한 상태인지 확인 (NONE 상태여야 함)
        if (dailyChallenge.status != Status.NONE) {
            throw PointException.CannotUsePoint
        }
        
        // 포인트 차감
        val updatedUser = userService.usePoint(userId, USAGE_POINT)
        
        // 일일 챌린지 상태를 FAILURE로 변경
        dailyChallengeService.changeStatusByCurrentStatus(dailyChallenge.copy(status = Status.FAILURE))
        
        return UsePointResponse(
            usedPoint = USAGE_POINT,
            totalPoint = updatedUser.point.toInt(),
            message = "포인트를 사용했습니다!"
        )
    }

    suspend fun getChallengePointStatusList(userId: Long): ChallengePointStatusListResponse {
        val challenges = challengeService.findChallengesByUserId(userId)
        
        val challengePointStatuses = challenges.map { challenge ->
            val dailyChallenges = dailyChallengeService.findDailyChallengesByUserIdAndChallengeId(userId, challenge.id)
            
            // 전체 일일 챌린지 중에서 EARNED 상태인 것들의 개수로 포인트 계산
            val earnedCount = dailyChallenges.count { it.status == Status.EARNED }
            val earnedPoint = earnedCount * EARNED_POINT
            
            // 포인트 획득/사용 가능 여부 판단
            val hasUnearned = dailyChallenges.any { it.status == Status.UNEARNED }
            val hasNone = dailyChallenges.any { it.status == Status.NONE }
            
            ChallengePointStatusResponse(
                challengeId = challenge.id,
                status = determineOverallStatus(dailyChallenges.map { it.status }),
                earnedPoint = earnedPoint,
                canEarnPoint = hasUnearned,
                canUsePoint = hasNone
            )
        }
        
        return ChallengePointStatusListResponse(challengePointStatuses)
    }

    fun getEarnedPoint(): EarnedPointResponse {
        return EarnedPointResponse(EARNED_POINT)
    }

    fun getUsagePoint(): UsagePointResponse {
        return UsagePointResponse(USAGE_POINT)
    }

    private fun determineOverallStatus(statuses: List<Status>): String {
        return when {
            statuses.all { it == Status.EARNED } -> "COMPLETED"
            statuses.any { it == Status.FAILURE } -> "FAILED"
            statuses.any { it == Status.EARNED || it == Status.UNEARNED } -> "IN_PROGRESS"
            else -> "NOT_STARTED"
        }
    }
}
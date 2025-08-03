package sopt.org.hmh.plugins

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // Repository 의존성
    single<sopt.org.hmh.domain.user.repository.UserRepository> { 
        sopt.org.hmh.domain.user.repository.UserRepositoryImpl() 
    }
    single<sopt.org.hmh.domain.challenge.repository.ChallengeRepository> { 
        sopt.org.hmh.domain.challenge.repository.ChallengeRepositoryImpl() 
    }
    
    // Service 의존성
    single<sopt.org.hmh.domain.user.service.UserService> { 
        sopt.org.hmh.domain.user.service.UserService(get()) 
    }
    single<sopt.org.hmh.domain.challenge.service.ChallengeService> { 
        sopt.org.hmh.domain.challenge.service.ChallengeService(get(), get()) 
    }
}
package sopt.org.hmh.plugins

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import io.ktor.serialization.kotlinx.json.json

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // HTTP Client
    single<io.ktor.client.HttpClient> {
        io.ktor.client.HttpClient(io.ktor.client.engine.cio.CIO) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(io.ktor.client.plugins.logging.Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.INFO
            }
        }
    }
    
    // JWT 의존성
    single<sopt.org.hmh.global.auth.jwt.JwtProvider> { 
        val jwtSecret = getProperty<String>("jwt.secret", "default-secret")
        sopt.org.hmh.global.auth.jwt.JwtProvider(jwtSecret) 
    }
    single<sopt.org.hmh.global.auth.jwt.TokenService> { 
        sopt.org.hmh.global.auth.jwt.TokenService(get()) 
    }
    
    // 소셜 로그인 의존성
    single<sopt.org.hmh.global.auth.social.kakao.KakaoLoginProvider> {
        sopt.org.hmh.global.auth.social.kakao.KakaoLoginProvider(
            httpClient = get(),
            clientId = getProperty<String>("kakao.client-id", ""),
            clientSecret = getProperty<String>("kakao.client-secret", ""),
            redirectUri = getProperty<String>("kakao.redirect-uri", "")
        )
    }
    single<sopt.org.hmh.global.auth.social.apple.AppleLoginProvider> {
        sopt.org.hmh.global.auth.social.apple.AppleLoginProvider(
            httpClient = get(),
            clientId = getProperty<String>("apple.client-id", ""),
            teamId = getProperty<String>("apple.team-id", ""),
            keyId = getProperty<String>("apple.key-id", ""),
            privateKey = getProperty<String>("apple.private-key", ""),
            redirectUri = getProperty<String>("apple.redirect-uri", "")
        )
    }
    single<sopt.org.hmh.global.auth.social.SocialLoginService> {
        sopt.org.hmh.global.auth.social.SocialLoginService(get(), get())
    }
    
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
    single<sopt.org.hmh.domain.auth.service.AuthService> {
        sopt.org.hmh.domain.auth.service.AuthService(get(), get(), get())
    }
}
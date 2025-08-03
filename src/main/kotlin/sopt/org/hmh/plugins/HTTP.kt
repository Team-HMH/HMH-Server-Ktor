package sopt.org.hmh.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
    
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("OS")
        allowHeader("Time-Zone")
        
        // 환경별 호스트 설정
        val allowedHosts = this@configureHTTP.environment.config.propertyOrNull("cors.allowedHosts")?.getString()
            ?: "localhost:3000,localhost:8080"
        allowedHosts.split(",").forEach { host ->
            allowHost(host.trim(), schemes = listOf("http", "https"))
        }
        
        allowCredentials = this@configureHTTP.environment.config.propertyOrNull("cors.allowCredentials")?.getString()?.toBoolean() ?: true
    }
    
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
}
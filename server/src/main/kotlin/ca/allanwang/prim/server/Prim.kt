package ca.allanwang.prim.server

import ca.allanwang.prim.models.About
import ca.allanwang.prim.server.routes.root
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.dsl.module.module
import org.koin.ktor.ext.installKoin
import java.text.DateFormat
import java.util.*

fun main(args: Array<String>) {
    embeddedServer(Netty,
            watchPaths = listOf("server", "models"),
            port = 8080,
            module = Application::primModule).start(true)
}

val aboutModule = module(createOnStart = true) {
    single { About(Date(), Date(), "test2", "test", emptyList()) }
}

fun Application.primModule() {
    install(DefaultHeaders)
    install(CallLogging)
    installKoin(listOf(aboutModule))

    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
        }
    }
    routing {
        root()
    }
}
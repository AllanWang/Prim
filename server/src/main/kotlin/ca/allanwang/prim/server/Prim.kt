package ca.allanwang.prim.server

import ca.allanwang.prim.server.routes.root
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.text.DateFormat

fun main(args: Array<String>) {
    embeddedServer(Netty,
            watchPaths = listOf("server"),
            port = 8080,
            module = Application::primModule).start(true)
}


fun Application.primModule() {
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
        }
    }
    routing {
        root()
    }
}
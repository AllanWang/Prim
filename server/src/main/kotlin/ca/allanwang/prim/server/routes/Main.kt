package ca.allanwang.prim.server.routes

import ca.allanwang.ktor.models.About
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.koin.core.scope.Scope
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject
import java.util.*

fun Routing.root() {
    get("/") {
        call.respondText("Hello World")
    }

    val about: About = get()

    get("/about") {
        call.respond(about)
    }
}
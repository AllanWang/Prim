package ca.allanwang.prim.server.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.root() {
    get("/") {
        call.respondText("Hello World")
    }
    get("/about") {
        call.respond("TODO about")
    }
}
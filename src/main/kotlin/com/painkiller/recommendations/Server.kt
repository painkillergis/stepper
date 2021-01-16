package com.painkiller.recommendations

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>) = main(args)

fun Application.applicationModule() {
  routing {
    get("/") {
      call.respondText("Hello, world!")
    }
  }
}

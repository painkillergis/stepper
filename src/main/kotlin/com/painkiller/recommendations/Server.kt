package com.painkiller.recommendations

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>) = main(args)

data class Version(val version: String)

fun Application.applicationModule() {
  routing {
    get("/") {
      call.respondText("Hello, world!")
    }
    VersionController().apply { routes() }
  }
  install(ContentNegotiation) {
    jackson()
  }
}

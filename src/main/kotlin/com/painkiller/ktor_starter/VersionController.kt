package com.painkiller.ktor_starter

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

class VersionController(var versionService: VersionService) {
  fun Application.module() {
    routing {
      get("/version") {
        call.respond(versionService.getVersion())
      }
    }
  }
}
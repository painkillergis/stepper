package com.painkiller.ktor_starter

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.versionController(versionService: VersionService) {
  routing {
    get("/version") {
      call.respond(versionService.getVersion())
    }
  }
}
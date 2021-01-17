package com.painkiller.recommendations

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

class VersionController {
  private val version: Version
  init {
    val properties = Properties()
    properties.load(javaClass.getResourceAsStream("/version.properties"))
    version = Version(properties.getProperty("version"))
  }

  fun Routing.routes() {
    get("/version") {
      call.respond(version)
    }
  }
}
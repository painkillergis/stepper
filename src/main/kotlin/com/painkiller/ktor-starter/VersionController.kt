package com.painkiller.ktor-starter

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*


class VersionController {
  data class Version(
    val sha: String,
    val version: String,
  )

  private val version: Version

  init {
    val properties = Properties()
    properties.load(javaClass.getResourceAsStream("/version.properties"))
    version = Version(
      properties.getProperty("sha"),
      properties.getProperty("version"),
    )
  }

  fun Routing.routes() {
    get("/version") {
      call.respond(version)
    }
  }
}
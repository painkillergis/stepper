package com.painkiller.ktor_starter

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>) = main(args)

fun Application.applicationModule() {
  val controllers = listOf(
    VersionController(
      VersionService(),
    ),
  )
  controllers.forEach { it.apply { module() } }
  globalModules()
}

fun Application.globalModules() {
  install(ContentNegotiation) {
    jackson()
  }
}

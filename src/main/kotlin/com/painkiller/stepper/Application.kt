package com.painkiller.stepper

import com.painkiller.stepper.dark_deployment.DarkDeployService
import com.painkiller.stepper.dark_deployment.darkDeployController
import com.painkiller.stepper.version.VersionService
import com.painkiller.stepper.version.versionController
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>) = main(args)

fun Application.applicationModule() {
  darkDeployController(
    DarkDeployService(
      DefaultKubernetesClient()
        .inNamespace("default"),
    ),
  )
  versionController(
    VersionService(),
  )
  globalModules()
}

fun Application.globalModules() {
  install(ContentNegotiation) {
    jackson()
  }
}

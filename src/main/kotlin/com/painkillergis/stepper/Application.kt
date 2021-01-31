package com.painkillergis.stepper

import com.painkillergis.stepper.deployment.*
import com.painkillergis.stepper.version.VersionService
import com.painkillergis.stepper.version.versionController
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>) = main(args)

fun Application.applicationModule() {
  val kubernetesClient = DefaultKubernetesClient() .inNamespace("default")
  deploymentController(
    DeploymentService(
      kubernetesClient,
    ),
    DeploymentSwitcherService(
      kubernetesClient,
    ),
    GroupAuthorizationService(),
    ServiceAccountService(
      kubernetesClient,
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

package com.painkiller.stepper

import com.painkiller.stepper.deployment.DeploymentService
import com.painkiller.stepper.deployment.DeploymentSwitcherService
import com.painkiller.stepper.deployment.ServiceAccountService
import com.painkiller.stepper.deployment.deploymentController
import com.painkiller.stepper.version.VersionService
import com.painkiller.stepper.version.versionController
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

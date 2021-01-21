package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.ServicePortBuilder

class Service(val serviceName: String, val deploymentName: String) : io.fabric8.kubernetes.api.model.Service() {
  init {
    metadata {
      name = serviceName
    }
    spec {
      type = "NodePort"
      ports = listOf(
        ServicePortBuilder()
          .withPort(8080)
          .withNewTargetPort(8080)
          .build()
      )
      selector = mapOf("app" to deploymentName)
    }
  }
}
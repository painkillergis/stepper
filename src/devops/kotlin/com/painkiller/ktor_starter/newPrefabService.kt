package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.newServicePort
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.IntOrString

fun newPrefabService(serviceName: String, deploymentName: String) =
  newService {
    metadata {
      name = serviceName
    }
    spec {
      type = "NodePort"
      ports = listOf(
        newServicePort {
          port = 8080
          targetPort = IntOrString(8080)
        }
      )
      selector = mapOf("app" to deploymentName)
    }
  }

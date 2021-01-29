package com.painkiller.stepper.deployment

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.*
import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.spec
import io.fabric8.kubernetes.api.model.IntOrString

fun newPrefabService(serviceName: String, deploymentName: String) = newService {
  metadata {
    name = serviceName
  }
  spec {
    type = "ClusterIP"
    ports = listOf(
      newServicePort {
        port = 8080
        targetPort = IntOrString(8080)
      }
    )
    selector = mapOf("app" to deploymentName)
  }
}

fun newPrefabDeployment(deploymentName: String, imageName: String, version: String) = newDeployment {
  metadata {
    name = deploymentName
    labels = mapOf("app" to deploymentName)
  }
  spec {
    selector {
      matchLabels = mapOf("app" to deploymentName)
    }
    template {
      metadata {
        labels = mapOf("app" to deploymentName)
      }
      spec {
        containers = listOf(
          newContainer {
            image = "painkillergis/$imageName:$version"
            name = deploymentName
            ports = listOf(
              newContainerPort {
                containerPort = 8080
                protocol = "TCP"
              }
            )
          }
        )
      }
    }
  }
}
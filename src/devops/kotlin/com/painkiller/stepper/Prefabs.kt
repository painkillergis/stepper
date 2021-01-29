package com.painkiller.stepper

import com.fkorotkov.kubernetes.*
import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.newDeployment
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.PodTemplateSpec
import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun newPrefabClient() = DefaultKubernetesClient().inNamespace("default")

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

fun newPrefabServiceAccount(serviceAccountName : String) = newServiceAccount {
  metadata {
    name = serviceAccountName
  }
}

fun newPrefabDeployment(deploymentName: String, podTemplateSpec: PodTemplateSpec) =
  newDeployment {
    metadata {
      name = deploymentName
      labels = mapOf("app" to deploymentName)
    }
    spec {
      replicas = 1
      selector {
        matchLabels = mapOf("app" to deploymentName)
      }
      template = podTemplateSpec
    }
  }

fun newPrefabPodTemplateSpec(group: String, imageName: String, deploymentName: String, version: String) =
  newPodTemplateSpec {
    metadata {
      labels = mapOf("app" to deploymentName)
    }
    spec {
      serviceAccount = "stepper-dark"
      containers = listOf(
        newContainer {
          name = deploymentName
          image = "$group/$imageName:$version"
          ports = listOf(
            newContainerPort {
              containerPort = 8080
            }
          )
        }
      )
    }
  }
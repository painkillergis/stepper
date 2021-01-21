package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.newDeployment
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec
import io.fabric8.kubernetes.api.model.PodTemplateSpec

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

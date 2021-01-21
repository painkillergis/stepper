package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec

class Deployment(deploymentName: String, podTemplateSpec: PodTemplateSpec) : io.fabric8.kubernetes.api.model.apps.Deployment() {
  init {
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
}

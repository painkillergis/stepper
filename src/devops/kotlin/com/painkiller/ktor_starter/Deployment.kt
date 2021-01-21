package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.apps.metadata
import com.fkorotkov.kubernetes.apps.selector
import com.fkorotkov.kubernetes.apps.spec

class Deployment(name: String, podTemplateSpec: PodTemplateSpec) : io.fabric8.kubernetes.api.model.apps.Deployment() {
  init {
    metadata {
      this.name = name
      labels = mapOf("app" to name)
    }
    spec {
      replicas = 1
      selector {
        matchLabels = mapOf("app" to name)
      }
      template = podTemplateSpec
    }
  }
}

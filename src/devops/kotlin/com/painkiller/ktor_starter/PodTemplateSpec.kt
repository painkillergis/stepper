package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newContainerPort
import com.fkorotkov.kubernetes.spec

class PodTemplateSpec(val group: String, val _name: String, val version: String) :
  io.fabric8.kubernetes.api.model.PodTemplateSpec() {
  init {
    metadata {
      labels = mapOf("app" to _name)
    }
    spec {
      containers = listOf(
        newContainer {
          name = _name
          image = "$group/$_name:$version"
          ports = listOf(
            newContainerPort {
              containerPort = 8080
            }
          )
        }
      )
    }
  }
}
package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newContainer
import com.fkorotkov.kubernetes.newContainerPort
import com.fkorotkov.kubernetes.spec

class PodTemplateSpec(val group: String, val imageName: String, val deploymentName: String, val version: String) :
  io.fabric8.kubernetes.api.model.PodTemplateSpec() {
  init {
    metadata {
      labels = mapOf("app" to deploymentName)
    }
    spec {
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
}
package com.painkiller.ktor_starter

import com.fkorotkov.kubernetes.*

fun newPrefabPodTemplateSpec(group: String, imageName: String, deploymentName: String, version: String) =
  newPodTemplateSpec {
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
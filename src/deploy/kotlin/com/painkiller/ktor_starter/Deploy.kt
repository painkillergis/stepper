package com.painkiller.ktor_starter

import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val (version) = args
  DefaultKubernetesClient().use {
    it
      .inNamespace("default")
      .apply {
        services().createOrReplace(Service("ktor-starter"))
        apps().deployments().createOrReplace(
          Deployment(
            "ktor-starter",
            PodTemplateSpec(
              "painkillergis",
              "ktor-starter",
              version,
            ),
          ),
        )
      }
  }
}
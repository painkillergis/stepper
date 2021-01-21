package com.painkiller.ktor_starter

import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val (group, name, version) = args
  DefaultKubernetesClient().use {
    it
      .inNamespace("default")
      .apply {
        services().createOrReplace(Service(name))
        apps().deployments().createOrReplace(
          Deployment(
            name,
            PodTemplateSpec(
              group,
              name,
              version,
            ),
          ),
        )
      }
  }
}
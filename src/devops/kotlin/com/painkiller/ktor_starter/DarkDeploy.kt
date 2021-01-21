package com.painkiller.ktor_starter

import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val (group, name, version) = args
  DefaultKubernetesClient().use {
    it
      .inNamespace("default")
      .apply {
        val isRed = services().withName(name).get()?.isRed() ?: false
        val darkDeploymentName = "$name-${if (isRed) "black" else "red"}"

        services().createOrReplace(
          newPrefabService(
            "$name-dark",
            darkDeploymentName,
          ),
        )

        apps().deployments().createOrReplace(
          newPrefabDeployment(
            darkDeploymentName,
            PodTemplateSpec(
              group,
              name,
              darkDeploymentName,
              version,
            ),
          ),
        )
      }
  }
}
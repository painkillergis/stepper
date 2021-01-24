package com.painkiller.stepper.dark_deployment

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class DarkDeployService(val kubernetesClient: NamespacedKubernetesClient) {
  fun createOrReplace(name: String, darkDeployment: DarkDeployment) {
    val deploymentName = kubernetesClient.services()
      .withName("$name-dark")
      .get()
      ?.spec
      ?.selector
      ?.get("app") ?: "$name-black"

    kubernetesClient.services().createOrReplace(
      newPrefabService("$name-dark", deploymentName)
    )

    kubernetesClient.apps().deployments().createOrReplace(
      newPrefabDeployment(name, deploymentName, darkDeployment.version)
    )
  }

  fun delete(name: String) {
    kubernetesClient.services().withName("$name-dark").delete()
  }
}

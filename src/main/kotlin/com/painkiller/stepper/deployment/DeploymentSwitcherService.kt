package com.painkiller.stepper.deployment

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class DeploymentSwitcherService(val kubernetesClient: NamespacedKubernetesClient) {
  fun switchDeployments(firstServiceName: String, lastServiceName: String) {
    val firstService = kubernetesClient.services().withName(firstServiceName)
    val lastService = kubernetesClient.services().withName(lastServiceName)

    val firstDeploymentName = firstService.get()?.spec?.selector?.get("app")
    val lastDeploymentName = lastService.get()?.spec?.selector?.get("app")

    if (firstDeploymentName == null && lastDeploymentName == null) {
      throw Error("No deployments available to switch")
    }

    kubernetesClient.services().createOrReplace(
      newPrefabService(firstServiceName, lastDeploymentName ?: "")
    )

    kubernetesClient.services().createOrReplace(
      newPrefabService(lastServiceName, firstDeploymentName ?: "")
    )
  }
}

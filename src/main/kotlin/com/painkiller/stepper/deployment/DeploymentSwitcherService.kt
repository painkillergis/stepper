package com.painkiller.stepper.deployment

import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.ServiceResource

class DeploymentSwitcherService(val kubernetesClient: NamespacedKubernetesClient) {
  fun switchDeployments(firstServiceName: String, lastServiceName: String) {
    val firstService = kubernetesClient.services().withName(firstServiceName)
    val lastService = kubernetesClient.services().withName(lastServiceName)

    val firstDeploymentName = deploymentName(firstService)
    val lastDeploymentName = deploymentName(lastService)

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

  private fun deploymentName(serviceResource: ServiceResource<Service>) =
    serviceResource.get()?.spec?.selector?.get("app")
}

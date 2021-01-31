package com.painkillergis.stepper.deployment

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class DeploymentService(val kubernetesClient: NamespacedKubernetesClient) {
  fun createOrReplace(serviceName: String, deployment: Deployment) {
    val deploymentName = if (
      kubernetesClient
        .services()
        .list().items
        .filter { it.spec?.selector?.get("app") == "$serviceName-black" }
        .any { it.metadata.name != serviceName }
    ) "$serviceName-red"
    else "$serviceName-black"

    kubernetesClient.serviceAccounts().createOrReplace(
      newPrefabServiceAccount(serviceName),
    )

    kubernetesClient.services().createOrReplace(
      newPrefabService(serviceName, deploymentName)
    )

    kubernetesClient.apps().deployments().createOrReplace(
      newPrefabDeployment(serviceName, deploymentName, deployment.group, deployment.imageName, deployment.version)
    )
  }

  fun delete(name: String) {
    kubernetesClient.services().withName(name)
      .apply {
        get()?.apply {
          kubernetesClient.apps().deployments()
            .withLabel("app", spec.selector["app"])
            .delete()
        }
      }
      .delete()
  }
}

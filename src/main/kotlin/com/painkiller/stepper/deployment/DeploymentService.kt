package com.painkiller.stepper.deployment

import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class DeploymentService(val kubernetesClient: NamespacedKubernetesClient) {
  fun createOrReplace(serviceName: String, deployment: Deployment) {
    val deploymentName = "$serviceName-${deployment.version.replace(".", "-")}"

    kubernetesClient.serviceAccounts().createOrReplace(
      newPrefabServiceAccount(serviceName),
    )

    kubernetesClient.services().createOrReplace(
      newPrefabService(serviceName, deploymentName)
    )

    kubernetesClient.apps().deployments().createOrReplace(
      newPrefabDeployment(serviceName, deploymentName, deployment.imageName, deployment.version)
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

  fun getServiceAccount(serviceName: String): String {
    return kubernetesClient.apps().deployments()
      .withLabel("app", kubernetesClient.services().withName(serviceName).get().spec.selector["app"])
      .list().items[0]
      .spec.template.spec.serviceAccount ?: "default"
  }

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

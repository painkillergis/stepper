package com.painkiller.stepper.deployment

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class DeploymentService(val kubernetesClient: NamespacedKubernetesClient) {
  fun createOrReplace(name: String, deployment: Deployment) {
    val deploymentName = "$name-${deployment.version.replace(".", "-")}"

    kubernetesClient.services().createOrReplace(
      newPrefabService(name, deploymentName)
    )

    kubernetesClient.apps().deployments().createOrReplace(
      newPrefabDeployment(deployment.imageName, deploymentName, deployment.version)
    )
  }

  fun delete(name: String) {
    val service = kubernetesClient.services().withName(name)
    kubernetesClient.apps().deployments()
      .withLabel("app", service.get().spec.selector["app"])
      .delete()
    service.delete()
  }

  fun getServiceAccount(serviceName: String) : String {
    return kubernetesClient.apps().deployments()
      .withLabel("app", kubernetesClient.services().withName(serviceName).get().spec.selector["app"])
      .list().items[0]
      .spec.template.spec.serviceAccount ?: "default"
  }
}

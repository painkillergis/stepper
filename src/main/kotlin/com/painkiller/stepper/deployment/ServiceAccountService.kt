package com.painkiller.stepper.deployment

import io.fabric8.kubernetes.client.NamespacedKubernetesClient

class ServiceAccountService(val kubernetesClient: NamespacedKubernetesClient) {
  fun getServiceAccount(serviceName: String): String {
    return kubernetesClient.apps().deployments()
      .withLabel("app", kubernetesClient.services().withName(serviceName).get().spec.selector["app"])
      .list().items[0]
      .spec.template.spec.serviceAccount ?: "default"
  }
}

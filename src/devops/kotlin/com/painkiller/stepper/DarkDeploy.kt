package com.painkiller.stepper

fun main(args: Array<String>) {
  val (group, name, version) = args
  val darkDeploymentName = "$name-dark-${version.replace(".", "-")}"
  newPrefabClient().use {
    it.serviceAccounts().createOrReplace(
      newPrefabServiceAccount("$name-dark"),
    )

    it.services().createOrReplace(
      newPrefabService(
        "$name-dark",
        darkDeploymentName,
      ),
    )

    it.apps().deployments().createOrReplace(
      newPrefabDeployment(
        darkDeploymentName,
        newPrefabPodTemplateSpec(
          group,
          name,
          darkDeploymentName,
          version,
        ),
      ),
    )
  }
}
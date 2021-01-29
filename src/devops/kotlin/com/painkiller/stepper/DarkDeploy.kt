package com.painkiller.stepper

fun main(args: Array<String>) {
  val (group, name, version) = args
  newPrefabClient().use {
    it
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
}
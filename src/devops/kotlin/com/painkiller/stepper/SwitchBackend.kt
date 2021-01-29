package com.painkiller.stepper

fun main(args: Array<String>) {
  val (name) = args
  newPrefabClient().use {
    val deploymentName = it.services().withName(name).get()?.spec?.selector?.get("app")
    val darkDeploymentName = it.services().withName("$name-dark").get()?.spec?.selector?.get("app")
      ?: throw Error("No dark deployment to promote!")

    it.services().createOrReplace(
      newPrefabService(
        name,
        darkDeploymentName
      ),
    )

    if (deploymentName != null) {
      it.services().createOrReplace(
        newPrefabService(
          "$name-dark",
          deploymentName,
        ),
      )
    }
  }
}
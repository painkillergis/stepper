package com.painkiller.stepper

fun main(args: Array<String>) {
  val (name) = args
  newPrefabClient().use {
    it
      .run {
        val darkDeploymentName = services().withName("$name-dark").get()?.spec?.selector?.get("app") ?: throw Error("No dark deployment to promote!")

        services().createOrReplace(
          newPrefabService(
            name,
            darkDeploymentName
          ),
        )

        services().createOrReplace(
          newPrefabService(
            "$name-dark",
            if (darkDeploymentName == "$name-black")  "$name-red"  else "$name-black",
          ),
        )
      }
  }
}
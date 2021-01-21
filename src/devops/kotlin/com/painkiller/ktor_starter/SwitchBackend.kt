package com.painkiller.ktor_starter

import io.fabric8.kubernetes.client.DefaultKubernetesClient

fun main(args: Array<String>) {
  val (name) = args
  DefaultKubernetesClient().use {
    it
      .inNamespace("default")
      .run {
        val isRed = services().withName(name).get()?.isRed() ?: false

        services().createOrReplace(
          Service(
            name,
            "$name-${if (isRed) "black" else "red"}",
          ),
        )

        services().createOrReplace(
          Service(
            "$name-dark",
            "$name-${if (isRed) "red" else "black"}",
          ),
        )
      }
  }
}
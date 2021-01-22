package com.painkiller.stepper

fun main(args: Array<String>) {
  val (name) = args
  newPrefabClient().use {
    it
      .run {
        val isRed = services().withName(name).get()?.isRed() ?: false

        services().createOrReplace(
          newPrefabService(
            name,
            "$name-${if (isRed) "black" else "red"}",
          ),
        )

        services().createOrReplace(
          newPrefabService(
            "$name-dark",
            "$name-${if (isRed) "red" else "black"}",
          ),
        )
      }
  }
}
package com.painkiller.stepper

import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val (serviceName, imageName, version) = args
  runBlocking {
    newHttpClient().post<Unit>("/services/$serviceName/deployment") {
      header("content-type", "application/json")
      body = mapOf(
        "imageName" to imageName,
        "version" to version,
      )
    }
  }
}

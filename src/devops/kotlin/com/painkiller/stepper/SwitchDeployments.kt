package com.painkiller.stepper

import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val (firstServiceName, lastServiceName) = args
  runBlocking {
    newHttpClient().post<Unit>("/services/$firstServiceName/switchDeploymentsWith/$lastServiceName")
  }
}

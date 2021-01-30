package com.painkiller.stepper

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*

fun newStepperClient() = HttpClient {
  install(JsonFeature)
  defaultRequest {
    url.protocol = URLProtocol.HTTP
    url.host = "painkiller.arctair.com"
    url.encodedPath = "/stepper/${url.encodedPath.trimStart('/')}"
  }
}

fun newStepperDarkClient() = HttpClient {
  install(JsonFeature)
  install(HttpTimeout) {
    socketTimeoutMillis = 4_000
  }
  defaultRequest {
    url.protocol = URLProtocol.HTTP
    url.host = "painkiller.arctair.com"
    url.encodedPath = "/stepper-dark/${url.encodedPath.trimStart('/')}"
  }
}

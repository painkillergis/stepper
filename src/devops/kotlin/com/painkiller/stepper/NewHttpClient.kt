package com.painkiller.stepper

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*

fun newHttpClient() = HttpClient {
  install(JsonFeature)
  defaultRequest {
    url.protocol = URLProtocol.HTTP
    url.host = "painkiller.arctair.com"
    url.encodedPath = "/stepper/${url.encodedPath.trimStart('/')}"
  }
}
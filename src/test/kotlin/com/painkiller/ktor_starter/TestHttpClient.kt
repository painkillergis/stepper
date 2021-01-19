package com.painkiller.ktor_starter

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

@Target(AnnotationTarget.FIELD)
annotation class TestHttpClient

object TestHttpClientProvider : BeforeEachCallback, ExtensionContext.Store.CloseableResource {

  var protocol = URLProtocol.HTTP
  var hostname = "localhost:8080"
  var basePath = ""

  init {
    val baseUrl = System.getenv("baseUrl")
    if (baseUrl != null) {
      val (protocolString, hostname, basePath) = Regex("(?<protocol>\\w+)://(?<hostname>[\\w.-]+(:\\d+)?)(?<basePath>.*)")
        .matchEntire(baseUrl)!!
        .groupValues
        .slice(listOf(1, 2, 4))
      protocol = URLProtocol.createOrDefault(protocolString)
      this.hostname = hostname
      this.basePath = basePath
    }
  }

  private val httpClient = HttpClient {
    install(JsonFeature)
    defaultRequest {
      url.protocol = protocol
      url.host = hostname
      url.encodedPath = if (url.encodedPath.isEmpty()) basePath else "$basePath/${url.encodedPath}"
    }
  }

  override fun beforeEach(context: ExtensionContext?) {
    val instance = context!!.requiredTestInstance
    instance.javaClass.declaredFields
      .filter { it.isAnnotationPresent(TestHttpClient::class.java) }
      .forEach { it.set(instance, httpClient) }
  }

  override fun close() {
    httpClient.close()
  }
}

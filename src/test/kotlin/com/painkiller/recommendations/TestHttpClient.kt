package com.painkiller.recommendations

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

@Target(AnnotationTarget.FIELD)
annotation class TestHttpClient

object TestHttpClientProvider : BeforeEachCallback, ExtensionContext.Store.CloseableResource {
  private val httpClient = HttpClient {
    install(JsonFeature)
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

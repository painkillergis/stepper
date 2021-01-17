package com.painkiller.recommendations

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@Extensions(
  ExtendWith(StartApplication::class),
  ExtendWith(TestHttpClientProvider::class),
)
class HelloWorldBSpec {

  @TestHttpClient
  lateinit var httpClient : HttpClient

  @Test
  fun `get hello world returns greetings`() {
    val bytes = runBlocking { httpClient.get<ByteArray>("/") }

    assertEquals("Hello, world!", String(bytes))
  }
}
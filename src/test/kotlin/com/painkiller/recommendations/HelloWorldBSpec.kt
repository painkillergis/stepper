package com.painkiller.recommendations

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@ExtendWith(StartApplication::class)
class HelloWorldBSpec {
  @Test
  fun `get hello world returns greetings`() {
    val client = HttpClient()

    val bytes = runBlocking { client.get<ByteArray>("http://localhost:8080") }

    assertEquals("Hello, world!", String(bytes))

    client.close()
  }
}
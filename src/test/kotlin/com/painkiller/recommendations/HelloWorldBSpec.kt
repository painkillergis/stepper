package com.painkiller.recommendations

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class HelloWorldBSpec {
  companion object {
    lateinit var server : NettyApplicationEngine

    @JvmStatic
    @BeforeAll
    fun beforeAll() {
      server = embeddedServer(
        Netty,
        applicationEngineEnvironment {
          module {
            applicationModule()
          }
          connector {
            port = 8080
          }
        },
      ).start()
    }

    @JvmStatic
    @AfterAll
    fun afterAll() {
      server.stop(1000, 10000)
    }
  }

  @Test
  fun `get hello world returns greetings`() {
    val client = HttpClient()

    val bytes = runBlocking { client.get<ByteArray>("http://localhost:8080") }

    assertEquals("Hello, world!", String(bytes))

    client.close()
  }
}
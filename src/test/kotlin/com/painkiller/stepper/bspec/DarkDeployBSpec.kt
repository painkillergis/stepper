package com.painkiller.stepper.bspec

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
class DarkDeployBSpec {

  @TestHttpClient()
  lateinit var stepperClient: HttpClient

  @TestHttpClient("target-dark")
  lateinit var targetDarkClient: HttpClient

  suspend fun <T> retry(times: Int, block: suspend () -> T): T {
    var delay = 250L
    repeat(times - 1) {
      try {
        return block()
      } catch (error: Error) {
        delay(delay)
        delay *= 2
      }
    }
    return block()
  }

  @Test
  fun `dark deploy`() {
    assertEquals(
      HttpStatusCode.OK,
      runBlocking {
        stepperClient.delete<HttpResponse>("/apps/stepper-target/darkDeployment").status
      }
    )

    runBlocking {
      retry(6) {
        assertEquals(
          HttpStatusCode.ServiceUnavailable,
          targetDarkClient.get<HttpResponse>("/version").status,
        )
      }
    }

    runBlocking {
      stepperClient.post<Unit> {
        url("/apps/stepper-target/darkDeployment")
        contentType(ContentType.Application.Json)
        body = mapOf("version" to "v0.0.3")
      }
    }

    runBlocking {
      retry(6) {
        val response = targetDarkClient.get<HttpResponse>("/version")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("v0.0.3", response.receive<Version>().version)
      }
    }
  }
}
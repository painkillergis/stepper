package com.painkiller.stepper.bspec

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@Extensions(
  ExtendWith(StartApplication::class),
  ExtendWith(TestHttpClientProvider::class),
)
internal class DeploymentBSpec {

  @TestHttpClient()
  lateinit var stepperClient: HttpClient

  @TestHttpClient("target-dark")
  lateinit var targetDarkClient: HttpClient

  @BeforeEach
  @AfterEach
  fun cleanup() {
    assertEquals(
      HttpStatusCode.OK,
      runBlocking {
        stepperClient.delete<HttpResponse>("/services/stepper-target-dark").status
      }
    )
  }

  @Test
  fun `create deployment`() {
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
        url("/services/stepper-target-dark/deployment")
        contentType(ContentType.Application.Json)
        body = mapOf(
          "imageName" to "stepper-target",
          "version" to "v0.0.3",
        )
      }
    }

    runBlocking {
      retry(7) {
        val response = targetDarkClient.get<HttpResponse>("/version")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("v0.0.3", response.receive<Version>().version)
      }
    }

    runBlocking {
      assertEquals(
        "stepper-target-dark",
        stepperClient.get("/services/stepper-target-dark/deployment/serviceAccount"),
      )
    }
  }
}

package com.painkillergis.stepper.bspec

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
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

  @TestHttpClient
  lateinit var stepperClient: HttpClient

  @TestHttpClient("target")
  lateinit var targetClient: HttpClient

  @TestHttpClient("target-dark")
  lateinit var targetDarkClient: HttpClient

  @BeforeEach
  @AfterEach
  fun cleanup() {
    assertEquals(
      HttpStatusCode.OK,
      runBlocking {
        stepperClient.delete<HttpResponse>("/services/stepper-target").status
      }
    )

    assertEquals(
      HttpStatusCode.OK,
      runBlocking {
        stepperClient.delete<HttpResponse>("/services/stepper-target-dark").status
      }
    )
  }

  @Test
  fun `create deployment not allowed for groups other than painkillergis`() {
    runBlocking {
      stepperClient
        .post<HttpResponse> {
          url("/services/stepper-target-dark/deployment")
          contentType(ContentType.Application.Json)
          body = mapOf(
            "group" to "notpainkillergis",
            "imageName" to "stepper-target",
            "version" to "v0.0.3",
          )
        }
        .apply { assertEquals(HttpStatusCode.BadRequest, status) }
    }
  }

  @Test
  fun `create deployment`() {
    runBlocking {
      retry(6) {
        targetDarkClient
          .get<HttpResponse>("/version")
          .apply { assertEquals(HttpStatusCode.ServiceUnavailable, status) }
      }
    }

    runBlocking {
      stepperClient
        .post<HttpResponse> {
          url("/services/stepper-target-dark/deployment")
          contentType(ContentType.Application.Json)
          body = mapOf(
            "group" to "painkillergis",
            "imageName" to "stepper-target",
            "version" to "v0.0.3",
          )
        }
        .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    runBlocking {
      retry(7) {
        targetDarkClient
          .get<HttpResponse>("/version")
          .apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("v0.0.3", receive<Version>().version)
          }
      }
    }

    runBlocking {
      stepperClient
        .get<HttpResponse>("/services/stepper-target-dark/deployment/serviceAccount")
        .apply {
          assertEquals(HttpStatusCode.OK, status)
          assertEquals("stepper-target-dark", content.toInputStream().bufferedReader().readText())
        }
    }

    runBlocking {
      stepperClient
        .post<HttpResponse>("/services/stepper-target/switchDeploymentsWith/stepper-target-dark")
        .apply { assertEquals(HttpStatusCode.OK, status) }
    }

    runBlocking {
      retry(7) {
        targetClient
          .get<HttpResponse>("/version")
          .apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("v0.0.3", receive<Version>().version)
          }

        targetDarkClient
          .get<HttpResponse>("/version")
          .apply { assertEquals(HttpStatusCode.ServiceUnavailable, status) }
      }
    }
  }
}

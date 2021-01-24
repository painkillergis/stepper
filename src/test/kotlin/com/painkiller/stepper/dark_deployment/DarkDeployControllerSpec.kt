package com.painkiller.stepper.dark_deployment

import com.painkiller.stepper.globalModules
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class DarkDeployControllerSpec {

  @MockK
  lateinit var darkDeployService: DarkDeployService

  private fun <R> withController(test: TestApplicationEngine.() -> R): R = withTestApplication(
    {
      darkDeployController(darkDeployService)
      globalModules()
    },
    test
  )

  @Test
  fun `post dark deploy`() = withController {
    every { darkDeployService.createOrReplace("anything", DarkDeployment("v1.2.3")) } returns Unit
    val call = handleRequest(method = HttpMethod.Post, uri = "/apps/anything/darkDeployment") {
      addHeader("content-type", "application/json")
      setBody(Json.encodeToString(DarkDeployment("v1.2.3")))
    }

    assertEquals(HttpStatusCode.OK, call.response.status())
  }


  @Test
  fun `post dark deploy failure`() = withController {
    every { darkDeployService.createOrReplace("anything", DarkDeployment("v1.2.3")) } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Post, uri = "/apps/anything/darkDeployment") {
      addHeader("content-type", "application/json")
      setBody(Json.encodeToString(DarkDeployment("v1.2.3")))
    }

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }


  @Test
  fun `delete dark deploy`() = withController {
    every { darkDeployService.delete("anything") } returns Unit
    val call = handleRequest(method = HttpMethod.Delete, uri = "/apps/anything/darkDeployment")

    assertEquals(HttpStatusCode.OK, call.response.status())
  }

  @Test
  fun `delete dark deploy failure`() = withController {
    every { darkDeployService.delete("anything") } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Delete, uri = "/apps/anything/darkDeployment")

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }
}
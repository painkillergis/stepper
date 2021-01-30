package com.painkiller.stepper.deployment

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
internal class DeploymentControllerSpec {

  @MockK
  lateinit var deploymentService: DeploymentService

  @MockK
  lateinit var serviceAccountService: ServiceAccountService

  private fun <R> withController(test: TestApplicationEngine.() -> R): R = withTestApplication(
    {
      deploymentController(
        deploymentService,
        serviceAccountService,
      )
      globalModules()
    },
    test
  )

  @Test
  fun `create service and deployment`() = withController {
    every { deploymentService.createOrReplace("anything", Deployment("imageName", "version")) } returns Unit
    val call = handleRequest(method = HttpMethod.Post, uri = "/services/anything/deployment") {
      addHeader("content-type", "application/json")
      setBody(Json.encodeToString(mapOf("imageName" to "imageName", "version" to "version")))
    }

    assertEquals(HttpStatusCode.OK, call.response.status())
  }


  @Test
  fun `create service and deployment failure`() = withController {
    every { deploymentService.createOrReplace("anything", Deployment("imageName", "version")) } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Post, uri = "/services/anything/deployment") {
      addHeader("content-type", "application/json")
      setBody(Json.encodeToString(mapOf("imageName" to "imageName", "version" to "version")))
    }

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }

  @Test
  fun `delete service and deployment`() = withController {
    every { deploymentService.delete("anything") } returns Unit
    val call = handleRequest(method = HttpMethod.Delete, uri = "/services/anything")

    assertEquals(HttpStatusCode.OK, call.response.status())
  }

  @Test
  fun `delete service and deployment failure`() = withController {
    every { deploymentService.delete("anything") } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Delete, uri = "/services/anything")

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }

  @Test
  fun `get deployment service account`() = withController {
    every { serviceAccountService.getServiceAccount("anything") } returns "the service account"
    val call = handleRequest(method = HttpMethod.Get, uri = "/services/anything/deployment/serviceAccount")

    assertEquals(HttpStatusCode.OK, call.response.status())
    assertEquals("the service account", call.response.content)
  }

  @Test
  fun `get deployment service account failure`() = withController {
    every { serviceAccountService.getServiceAccount("anything") } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Get, uri = "/services/anything/deployment/serviceAccount")

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }

  @Test
  fun `swap backends`() = withController {
    every { deploymentService.switchDeployments("service1", "service2") } returns Unit
    val call = handleRequest(method = HttpMethod.Post, uri = "/services/service1/switchDeploymentsWith/service2")

    assertEquals(HttpStatusCode.OK, call.response.status())
  }

  @Test
  fun `swap backends failure`() = withController {
    every { deploymentService.switchDeployments("service1", "service2") } throws Error("failure")
    val call = handleRequest(method = HttpMethod.Post, uri = "/services/service1/switchDeploymentsWith/service2")

    assertEquals(HttpStatusCode.InternalServerError, call.response.status())
  }
}
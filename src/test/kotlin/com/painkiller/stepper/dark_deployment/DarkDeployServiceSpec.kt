package com.painkiller.stepper.dark_deployment

import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceList
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.RollableScalableResource
import io.fabric8.kubernetes.client.dsl.ServiceResource
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class DarkDeployServiceSpec {

  private val services = mockk<MixedOperation<Service, ServiceList, ServiceResource<Service>>>(relaxed = true) { }

  private val deployments =
    mockk<MixedOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>>>(relaxed = true) {}

  private val kubernetesClient = mockk<NamespacedKubernetesClient> {
    every { services() } returns services
    every { apps() } returns mockk {
      every { deployments() } returns deployments
    }
  }

  @Test
  fun `create dark service and black deployment when service does not exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name-dark")
    } returns mockk(relaxed = true) {
      every { get() } returns null
    }

    DarkDeployService(kubernetesClient).createOrReplace("app-name", DarkDeployment("v5.4.3"))

    excludeRecords { services.withName(any()) }
    verify {
      services.createOrReplace(
        newPrefabService("app-name-dark", "app-name-black")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-black", "v5.4.3")
      )
    }
    confirmVerified(services)
    confirmVerified(deployments)
  }

  @Test
  fun `replace dark service and black deployment when service is wired to black deployment`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name-dark")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "app-name-black")
        }
      }
    }

    DarkDeployService(kubernetesClient).createOrReplace("app-name", DarkDeployment("v5.4.3"))

    excludeRecords { services.withName(any()) }
    verify {
      services.createOrReplace(
        newPrefabService("app-name-dark", "app-name-black")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-black", "v5.4.3")
      )
    }

    confirmVerified(services)
  }

  @Test
  fun `replace dark service and black deployment when service is wired to red deployment`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name-dark")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "app-name-red")
        }
      }
    }

    DarkDeployService(kubernetesClient).createOrReplace("app-name", DarkDeployment("v5.4.3"))

    excludeRecords { services.withName(any()) }
    verify {
      services.createOrReplace(
        newPrefabService("app-name-dark", "app-name-red")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-red", "v5.4.3")
      )
    }

    confirmVerified(services)
  }

  @Test
  fun `delete dark service`() {
    val service = mockk<ServiceResource<Service>>(relaxed = true) {}
    every {
      hint(ServiceResource::class)
      services.withName("app-name-dark")
    } returns service

    DarkDeployService(kubernetesClient).delete("app-name")

    verify { service.delete() }

    confirmVerified(service)
  }
}


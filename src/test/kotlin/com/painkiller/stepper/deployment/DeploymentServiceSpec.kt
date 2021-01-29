package com.painkiller.stepper.deployment

import com.fkorotkov.kubernetes.apps.newDeployment
import com.fkorotkov.kubernetes.apps.spec
import com.fkorotkov.kubernetes.apps.template
import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceList
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.RollableScalableResource
import io.fabric8.kubernetes.client.dsl.ServiceResource
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class DeploymentServiceSpec {

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
  fun `create dark service and black deployment when they don't exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk(relaxed = true) {
      every { get() } returns null
    }

    DeploymentService(kubernetesClient).createOrReplace(
      "app-name",
      Deployment("imageName", "v5.4.3")
    )

    excludeRecords { services.withName(any()) }
    verify {
      services.createOrReplace(
        newPrefabService("app-name", "app-name-v5-4-3")
      )
      deployments.createOrReplace(
        newPrefabDeployment("imageName", "app-name-v5-4-3", "v5.4.3")
      )
    }
    confirmVerified(services)
    confirmVerified(deployments)
  }

  @Test
  fun `replace service and deployment when they exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "app-name-v5-4-3")
        }
      }
    }

    DeploymentService(kubernetesClient).createOrReplace(
      "app-name",
      Deployment("imageName", "v5.4.3")
    )

    excludeRecords { services.withName(any()) }
    verify {
      services.createOrReplace(
        newPrefabService("app-name", "app-name-v5-4-3")
      )
      deployments.createOrReplace(
        newPrefabDeployment("imageName", "app-name-v5-4-3", "v5.4.3")
      )
    }

    confirmVerified(services)
  }

  @Test
  fun `delete service and deployment`() {
    val service = mockk<ServiceResource<Service>>(relaxed = true) {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "the deployment label")
        }
      }
    }
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns service

    val deploymentsMatchingLabel = mockk<FilterWatchListDeletable<Deployment, DeploymentList>>(relaxed = true) {}
    every {
      deployments.withLabel("app", "the deployment label")
    } returns deploymentsMatchingLabel

    DeploymentService(kubernetesClient).delete("app-name")

    verify {
      service.delete()
      deploymentsMatchingLabel.delete()
    }

    excludeRecords { service.get() }
    confirmVerified(service)
    confirmVerified(deploymentsMatchingLabel)
  }

  @Test
  fun `get service account`() {
    val service = mockk<ServiceResource<Service>>(relaxed = true) {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "the deployment label")
        }
      }
    }
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns service

    val deploymentsMatchingLabel = mockk<FilterWatchListDeletable<Deployment, DeploymentList>>(relaxed = true) {
      every { list() } returns mockk {
        every { items } returns listOf(
          newDeployment {
            spec {
              template {
                spec {
                  serviceAccount = "the service account"
                }
              }
            }
          },
        )
      }
    }
    every {
      deployments.withLabel("app", "the deployment label")
    } returns deploymentsMatchingLabel

    assertEquals(
      "the service account",
      DeploymentService(kubernetesClient).getServiceAccount("app-name"),
    )
  }

  @Test
  fun `get default service account`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk(relaxed = true) {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "the deployment label")
        }
      }
    }

    every {
      deployments.withLabel("app", "the deployment label")
    } returns mockk(relaxed = true) {
      every { list() } returns mockk {
        every { items } returns listOf(
          newPrefabDeployment("imageName", "deploymentName", "version"),
        )
      }
    }

    assertEquals(
      "default",
      DeploymentService(kubernetesClient).getServiceAccount("app-name"),
    )
  }
}


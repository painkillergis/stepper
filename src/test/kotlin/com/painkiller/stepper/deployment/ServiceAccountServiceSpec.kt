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
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class ServiceAccountServiceSpec {

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
          newPrefabDeployment("the service account", "", "", ""),
        )
      }
    }
    every {
      deployments.withLabel("app", "the deployment label")
    } returns deploymentsMatchingLabel

    assertEquals(
      "the service account",
      ServiceAccountService(kubernetesClient).getServiceAccount("app-name"),
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
          newDeployment {
            spec {
              template {
                spec { }
              }
            }
          },
        )
      }
    }

    assertEquals(
      "default",
      ServiceAccountService(kubernetesClient).getServiceAccount("app-name"),
    )
  }
}


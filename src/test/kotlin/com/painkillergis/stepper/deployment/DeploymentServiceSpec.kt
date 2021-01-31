package com.painkillergis.stepper.deployment

import com.fkorotkov.kubernetes.*
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.*
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class DeploymentServiceSpec {

  private val services = mockk<MixedOperation<Service, ServiceList, ServiceResource<Service>>>(relaxed = true) { }

  private val deployments =
    mockk<MixedOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>>>(relaxed = true) {}

  private val serviceAccounts =
    mockk<MixedOperation<ServiceAccount, ServiceAccountList, Resource<ServiceAccount>>>(relaxed = true) {}

  private val kubernetesClient = mockk<NamespacedKubernetesClient> {
    every { services() } returns services
    every { apps() } returns mockk {
      every { deployments() } returns deployments
    }
    every { serviceAccounts() } returns serviceAccounts
  }

  @BeforeEach
  fun setup() {
    excludeRecords {
      services.list()
      services.withName(any())
    }
  }

  @AfterEach
  fun verify() {
    confirmVerified(services)
    confirmVerified(deployments)
    confirmVerified(serviceAccounts)
  }

  @Test
  fun `create service, deployment, and service account when they don't exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk(relaxed = true) {
      every { get() } returns null
    }

    every { services.list() } returns mockk {
      every { items } returns emptyList()
    }

    DeploymentService(kubernetesClient).createOrReplace(
      "app-name",
      Deployment("groupName", "imageName", "v5.4.3")
    )

    verify {
      services.createOrReplace(
        newPrefabService("app-name", "app-name-black")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-black", "groupName", "imageName", "v5.4.3")
      )
      serviceAccounts.createOrReplace(
        newPrefabServiceAccount("app-name"),
      )
    }
  }

  @Test
  fun `replace service and deployment when another services owns the default deployment name`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk {
      every { get() } returns null
    }

    every {
      services.list()
    } returns mockk {
      every { items } returns listOf(
        newPrefabService("app-name-other", "app-name-black"),
        newService {
          metadata {
            name = "app-name-other-2"
          }
          spec {}
        },
      )
    }

    DeploymentService(kubernetesClient).createOrReplace(
      "app-name",
      Deployment("groupName", "imageName", "v5.4.3")
    )

    verify {
      services.createOrReplace(
        newPrefabService("app-name", "app-name-red")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-red", "groupName", "imageName", "v5.4.3")
      )
      serviceAccounts.createOrReplace(
        newPrefabServiceAccount("app-name"),
      )
    }
  }

  @Test
  fun `replace service and deployment when they exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "app-name-black")
        }
      }
    }

    every {
      services.list()
    } returns mockk {
      every { items } returns listOf(
        newPrefabService("app-name", "app-name-black"),
      )
    }

    DeploymentService(kubernetesClient).createOrReplace(
      "app-name",
      Deployment("groupName", "imageName", "v5.4.3")
    )

    verify {
      services.createOrReplace(
        newPrefabService("app-name", "app-name-black")
      )
      deployments.createOrReplace(
        newPrefabDeployment("app-name", "app-name-black", "groupName", "imageName", "v5.4.3")
      )
      serviceAccounts.createOrReplace(
        newPrefabServiceAccount("app-name"),
      )
    }
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

    excludeRecords {
      service.get()
      deployments.withLabel(any(), any())
    }
    confirmVerified(service)
    confirmVerified(deploymentsMatchingLabel)
  }

  @Test
  fun `delete is noop when service and deployment don't exist`() {
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns mockk(relaxed = true) {
      every { get() } returns null
    }

    DeploymentService(kubernetesClient).delete("app-name")
  }
}


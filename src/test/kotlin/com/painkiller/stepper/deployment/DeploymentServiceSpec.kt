package com.painkiller.stepper.deployment

import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceAccount
import io.fabric8.kubernetes.api.model.ServiceAccountList
import io.fabric8.kubernetes.api.model.ServiceList
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentList
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.*
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.assertThrows
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

  @Test
  fun `create service, deployment, and service account when they don't exist`() {
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
        newPrefabDeployment("app-name", "app-name-v5-4-3", "imageName", "v5.4.3")
      )
      serviceAccounts.createOrReplace(
        newPrefabServiceAccount("app-name"),
      )
    }

    confirmVerified(services)
    confirmVerified(deployments)
    confirmVerified(serviceAccounts)
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
        newPrefabDeployment("app-name", "app-name-v5-4-3", "imageName", "v5.4.3")
      )
      serviceAccounts.createOrReplace(
        newPrefabServiceAccount("app-name"),
      )
    }

    confirmVerified(services)
    confirmVerified(deployments)
    confirmVerified(serviceAccounts)
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
  fun `delete is noop when service and deployment don't exist`() {
    val service = mockk<ServiceResource<Service>>(relaxed = true) {
      every { get() } returns null
    }
    every {
      hint(ServiceResource::class)
      services.withName("app-name")
    } returns service

    DeploymentService(kubernetesClient).delete("app-name")
  }

  @Test
  fun `switch backends`() {
    every {
      hint(ServiceResource::class)
      services.withName("firstServiceName")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "firstDeploymentName")
        }
      }
    }

    every {
      hint(ServiceResource::class)
      services.withName("lastServiceName")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "lastDeploymentName")
        }
      }
    }

    DeploymentService(kubernetesClient).switchDeployments(
      "firstServiceName",
      "lastServiceName",
    )

    verify {
      services.createOrReplace(
        newPrefabService("firstServiceName", "lastDeploymentName"),
      )
      services.createOrReplace(
        newPrefabService("lastServiceName", "firstDeploymentName"),
      )
    }
  }

  @Test
  fun `switch backends when last is missing`() {
    every {
      hint(ServiceResource::class)
      services.withName("firstServiceName")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "firstDeploymentName")
        }
      }
    }

    every {
      hint(ServiceResource::class)
      services.withName("lastServiceName")
    } returns mockk {
      every { get() } returns null
    }

    DeploymentService(kubernetesClient).switchDeployments(
      "firstServiceName",
      "lastServiceName",
    )

    verify {
      services.createOrReplace(
        newPrefabService("firstServiceName", ""),
      )
      services.createOrReplace(
        newPrefabService("lastServiceName", "firstDeploymentName"),
      )
    }
  }

  @Test
  fun `switch backends when first is missing`() {
    every {
      hint(ServiceResource::class)
      services.withName("firstServiceName")
    } returns mockk {
      every { get() } returns null
    }

    every {
      hint(ServiceResource::class)
      services.withName("lastServiceName")
    } returns mockk {
      every { get() } returns newService {
        spec {
          selector = mapOf("app" to "lastDeploymentName")
        }
      }
    }

    DeploymentService(kubernetesClient).switchDeployments(
      "firstServiceName",
      "lastServiceName",
    )

    verify {
      services.createOrReplace(
        newPrefabService("firstServiceName", "lastDeploymentName"),
      )
      services.createOrReplace(
        newPrefabService("lastServiceName", ""),
      )
    }
  }

  @Test
  fun `switch backends when both are missing`() {
    every {
      hint(ServiceResource::class)
      services.withName("firstServiceName")
    } returns mockk {
      every { get() } returns null
    }

    every {
      hint(ServiceResource::class)
      services.withName("lastServiceName")
    } returns mockk {
      every { get() } returns null
    }

    assertThrows<Error>("No deployments available to switch") {
      DeploymentService(kubernetesClient).switchDeployments(
        "firstServiceName",
        "lastServiceName",
      )
    }
  }
}


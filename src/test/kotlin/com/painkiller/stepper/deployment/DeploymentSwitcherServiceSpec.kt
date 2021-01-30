package com.painkiller.stepper.deployment

import com.fkorotkov.kubernetes.newService
import com.fkorotkov.kubernetes.spec
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceList
import io.fabric8.kubernetes.client.NamespacedKubernetesClient
import io.fabric8.kubernetes.client.dsl.MixedOperation
import io.fabric8.kubernetes.client.dsl.ServiceResource
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class DeploymentSwitcherServiceSpec {

  private val services = mockk<MixedOperation<Service, ServiceList, ServiceResource<Service>>>(relaxed = true) { }

  private val kubernetesClient = mockk<NamespacedKubernetesClient> {
    every { services() } returns services
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

    DeploymentSwitcherService(kubernetesClient).switchDeployments(
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

    DeploymentSwitcherService(kubernetesClient).switchDeployments(
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

    DeploymentSwitcherService(kubernetesClient).switchDeployments(
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
      DeploymentSwitcherService(kubernetesClient).switchDeployments(
        "firstServiceName",
        "lastServiceName",
      )
    }
  }
}


package com.painkiller.stepper

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newServiceAccount
import com.fkorotkov.kubernetes.rbac.*

fun main(args: Array<String>) {
  val (group, appName, version) = args
  newPrefabClient().use {
    val darkServiceName = "$appName-dark"
    val darkDeploymentName = "$appName-black"

    it.serviceAccounts().createOrReplace(
      newServiceAccount {
        metadata {
          name = appName
        }
      }
    )

    it.rbac().roles().createOrReplace(
      newRole {
        metadata {
          name = appName
        }
        rules = listOf(
          newPolicyRule {
            apiGroups = listOf("")
            resources = listOf("services")
            verbs = listOf("create", "get", "delete")
          },
          newPolicyRule {
            apiGroups = listOf("apps")
            resources = listOf("deployments")
            verbs = listOf("create", "get", "update")
          }
        )
      }
    )

    it.rbac().roleBindings().createOrReplace(
      newRoleBinding {
        metadata {
          name = appName
        }
        subjects = listOf(
          newSubject {
            kind = "ServiceAccount"
            name = appName
          }
        )
        roleRef {
          kind = "Role"
          name = appName
        }
      }
    )

    it.services().create(
      newPrefabService(
        darkServiceName,
        darkDeploymentName,
      ),
    )

    it.apps().deployments().create(
      newPrefabDeployment(
        darkDeploymentName,
        newPrefabPodTemplateSpec(
          group,
          appName,
          darkDeploymentName,
          version,
        ),
      ),
    )
  }
}
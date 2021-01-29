package com.painkiller.stepper

import com.fkorotkov.kubernetes.rbac.*

fun main(args: Array<String>) {
  val (appName) = args
  newPrefabClient().use {
    it.rbac().roles().createOrReplace(
      newRole {
        metadata {
          name = appName
        }
        rules = listOf(
          newPolicyRule {
            apiGroups = listOf("")
            resources = listOf("services")
            verbs = listOf("create", "delete", "get", "update")
          },
          newPolicyRule {
            apiGroups = listOf("")
            resources = listOf("serviceaccounts")
            verbs = listOf("create", "get", "update")
          },
          newPolicyRule {
            apiGroups = listOf("apps")
            resources = listOf("deployments")
            verbs = listOf("create", "delete", "get", "list", "update")
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
            namespace = "default"
            name = "$appName-dark"
          },
        )
        roleRef {
          kind = "Role"
          name = appName
        }
      }
    )
  }
}
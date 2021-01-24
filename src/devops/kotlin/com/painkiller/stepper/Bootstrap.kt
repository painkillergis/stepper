package com.painkiller.stepper

import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newServiceAccount
import com.fkorotkov.kubernetes.rbac.*

fun main(args: Array<String>) {
  val (appName) = args
  newPrefabClient().use {
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
  }
}
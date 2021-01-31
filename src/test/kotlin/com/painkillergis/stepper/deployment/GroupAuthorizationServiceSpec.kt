package com.painkillergis.stepper.deployment

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

internal class GroupAuthorizationServiceSpec : StringSpec() {
  init {
    val groupAuthorizationService = GroupAuthorizationService()

    "authorize some groups" {
      table(
        headers("group", "is authorized"),
        row("painkillergis", true),
        row("arctair", true),
        row("anything else", false),
      ).forAll {group, isAuthorized ->
        groupAuthorizationService.isGroupAuthorized(group) shouldBe isAuthorized
      }
    }
  }
}
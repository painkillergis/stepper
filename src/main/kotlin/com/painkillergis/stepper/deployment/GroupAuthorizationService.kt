package com.painkillergis.stepper.deployment

class GroupAuthorizationService {
  fun isGroupAuthorized(group: String) =
    group in listOf("painkillergis", "arctair")
}

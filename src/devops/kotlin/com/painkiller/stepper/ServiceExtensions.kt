package com.painkiller.stepper

fun io.fabric8.kubernetes.api.model.Service.isRed(): Boolean {
  return spec.selector["app"]?.endsWith("-red") ?: false
}


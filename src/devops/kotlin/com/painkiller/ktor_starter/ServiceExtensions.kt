package com.painkiller.ktor_starter

fun io.fabric8.kubernetes.api.model.Service.isRed(): Boolean {
  return spec.selector["app"]?.endsWith("-red") ?: false
}


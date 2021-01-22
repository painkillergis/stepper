package com.painkiller.stepper

import java.util.*

class VersionService {

  private val version: Version

  init {
    val properties = Properties()
    properties.load(javaClass.getResourceAsStream("/version.properties"))
    version = Version(
      properties.getProperty("sha"),
      properties.getProperty("version"),
    )
  }

  fun getVersion(): Version {
    return version
  }
}
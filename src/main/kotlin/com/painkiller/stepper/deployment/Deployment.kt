package com.painkiller.stepper.deployment

import kotlinx.serialization.Serializable

@Serializable
data class Deployment(val group : String, val imageName : String, val version: String)

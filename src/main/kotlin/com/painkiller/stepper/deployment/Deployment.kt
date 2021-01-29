package com.painkiller.stepper.deployment

import kotlinx.serialization.Serializable

@Serializable
data class Deployment(val imageName : String, val version: String)

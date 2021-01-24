package com.painkiller.stepper.dark_deployment

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.darkDeployController(darkDeployService: DarkDeployService) {
  routing {
    post("/apps/{name}/darkDeployment") {
      try {
        darkDeployService.createOrReplace(
          call.parameters["name"]!!,
          call.receive()
        )
        call.respond(HttpStatusCode.OK)
      } catch (error: Error) {
        call.application.environment.log.error(error.message)
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
    delete("/apps/{name}/darkDeployment") {
      try {
        darkDeployService.delete(call.parameters["name"]!!)
        call.respond(HttpStatusCode.OK)
      } catch (error: Error) {
        call.application.environment.log.error(error.message)
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
  }
}
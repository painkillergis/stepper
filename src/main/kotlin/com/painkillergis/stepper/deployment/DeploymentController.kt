package com.painkillergis.stepper.deployment

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.deploymentController(
  deploymentService: DeploymentService,
  deploymentSwitcherService: DeploymentSwitcherService,
  groupAuthorizationService: GroupAuthorizationService,
  serviceAccountService: ServiceAccountService,
) {
  routing {
    post("/services/{name}/deployment") {
      val deployment = call.receive<Deployment>()
      if (!groupAuthorizationService.isGroupAuthorized(deployment.group)) {
        call.respond(HttpStatusCode.BadRequest, "group not allowed")
      } else {
        try {
          deploymentService.createOrReplace(
            call.parameters["name"]!!,
            deployment,
          )
          call.respond(HttpStatusCode.OK)
        } catch (error: Error) {
          call.application.environment.log.error(error.message)
          call.respond(HttpStatusCode.InternalServerError)
        }
      }
    }
    delete("/services/{name}") {
      try {
        deploymentService.delete(call.parameters["name"]!!)
        call.respond(HttpStatusCode.OK)
      } catch (error: Error) {
        call.application.environment.log.error(error.message)
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
    get("/services/{name}/deployment/serviceAccount") {
      try {
        call.respond(serviceAccountService.getServiceAccount(call.parameters["name"]!!))
      } catch (error: Error) {
        call.application.environment.log.error(error.message)
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
    post("/services/{firstServiceName}/switchDeploymentsWith/{lastServiceName}") {
      try {
        deploymentSwitcherService.switchDeployments(
          call.parameters["firstServiceName"]!!,
          call.parameters["lastServiceName"]!!,
        )
        call.respond(HttpStatusCode.OK)
      } catch (error: Error) {
        call.application.environment.log.error(error.message)
        call.respond(HttpStatusCode.InternalServerError)
      }
    }
  }
}
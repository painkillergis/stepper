# ktor-starter
This starter gifts you a runnable barebones Kotlin Ktor application with tests.
## Eject
`./eject`
Ejecting renames packages, artifacts, services, deployments, and the project name from ktor-starter to the name of this directory.
## Run the tests
### From command line
`./gradlew test`
### From IntelliJ
Right click src/test directory and click Run
## Deploy
Deployment scripts to Kubernetes are included in the scripts/ directory.
`scripts/deploy`
The deploy script builds and pushes a new Docker image, creates or updates your Kubernetes service and deployment, and waits for changes to be visible at the deployment URL.

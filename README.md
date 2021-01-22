# stepper
Stepper enables dark rollout automation.
## Run the tests
`./gradlew cleanTest test`
### Against a deployment
`baseUrl=http://painkiller.arctair.com/stepper ./gradlew cleanTest test`
## Build, deploy, verify
`scripts/ci`
The ci script executes these steps:
1. Build jar file
1. Build and push Docker image
1. Create or update dark (non-production) Kubernetes service and deployment
1. Run blackbox tests against dark deployment baseUrl
1. Swap dark deployment and live deployment

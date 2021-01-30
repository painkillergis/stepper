import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  application
  kotlin("jvm") version "1.4.21"
  kotlin("plugin.serialization") version "1.4.21"
  id("com.github.johnrengelman.shadow") version "4.0.4"
  id("com.palantir.docker") version "0.25.0"
}

application {
  mainClassName = "${packageBase()}.ApplicationKt"
}

group = "com.painkiller"
val major = 1
val minor = 0

fun safeName(): String {
  return rootProject.name.replace("-", "_")
}

fun packageBase(): String {
  return "com.painkiller.${safeName()}"
}

repositories {
  jcenter()
  mavenCentral()
  maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
  maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}

sourceSets.create("devops").java.srcDir("src/deploy/kotlin")

dependencies {
  "devopsImplementation"("com.fkorotkov:kubernetes-dsl:+")
  "devopsImplementation"("io.fabric8:kubernetes-client:+")
  "devopsImplementation"("io.ktor:ktor-client-apache:+")
  "devopsImplementation"("io.ktor:ktor-client-core:+")
  "devopsImplementation"("io.ktor:ktor-client-jackson:+")
  "devopsImplementation"("org.jetbrains.kotlin:kotlin-reflect:+")
  "devopsImplementation"("org.jetbrains.kotlin:kotlin-stdlib:+")
  "devopsImplementation"("org.slf4j:slf4j-simple:+")
  implementation("com.fkorotkov:kubernetes-dsl:+")
  implementation("io.fabric8:kubernetes-client:+")
  implementation("io.ktor:ktor-html-builder:+")
  implementation("io.ktor:ktor-jackson:+")
  implementation("io.ktor:ktor-server-netty:+")
  implementation("org.jetbrains.kotlin:kotlin-reflect:+")
  implementation("org.jetbrains.kotlin:kotlin-stdlib:+")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:+")
  implementation("org.slf4j:slf4j-simple:+")
  testImplementation("io.ktor:ktor-client-apache:+")
  testImplementation("io.ktor:ktor-client-core:+")
  testImplementation("io.ktor:ktor-client-jackson:+")
  testImplementation("io.ktor:ktor-server-test-host:+")
  testImplementation("io.mockk:mockk:+")
  testImplementation("org.hamcrest:hamcrest-junit:+")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+")
  testImplementation("org.junit.jupiter:junit-jupiter-api:+")
  testImplementation(kotlin("test-junit5"))
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:+")
}

tasks.test {
  useJUnitPlatform()
  if (System.getenv("stepper_baseUrl") != null || System.getProperty("bspec") != null) {
    include("**/bspec/")
  }
}

tasks.withType<KotlinCompile>() {
  kotlinOptions.jvmTarget = "11"
}

tasks.named("processResources") {
  doFirst {
    file("src/main/resources/version.properties")
      .writeText("sha=${getSha()}\nversion=${getVersion()}")
  }
}

fun getSha(): String {
  return ProcessBuilder("sh", "-c", "git rev-parse HEAD")
    .start()
    .apply { waitFor() }
    .let { it.inputStream.bufferedReader().readText().trim() }
}

fun getVersion(): String {
  return ProcessBuilder("sh", "-c", "git rev-list --count HEAD")
    .start()
    .apply { waitFor() }
    .let { it.inputStream.bufferedReader().readText().trim() }
    .let { "v$major.$minor.$it" }
}

configurations.all {
  resolutionStrategy {
    activateDependencyLocking()
    componentSelection
      .all(object : Action<ComponentSelection> {
        @Mutate
        override fun execute(selection: ComponentSelection) {
          val version = selection.candidate.version
          when {
            version.matches(
              Regex(
                ".*-rc$",
                RegexOption.IGNORE_CASE
              )
            ) -> selection.reject("Release candidates are excluded")
            version.matches(Regex(".*-M\\d+$")) -> selection.reject("Milestones are excluded")
            version.matches(Regex(".*-alpha\\d+$")) -> selection.reject("Alphas are excluded")
            version.matches(Regex(".*-beta\\d+$")) -> selection.reject("Betas are excluded")
          }
        }
      })
  }
}

docker {
  name = "painkillergis/${rootProject.name}:${getVersion()}"
  files("build/libs/${rootProject.name}.jar")
}

val bootstrap by tasks.registering(JavaExec::class) {
  main = "${packageBase()}.BootstrapKt"
  classpath = sourceSets["devops"].runtimeClasspath
  args = listOf(rootProject.name)
}

val darkDeploy by tasks.registering(JavaExec::class) {
  main = "${packageBase()}.DeploymentKt"
  classpath = sourceSets["devops"].runtimeClasspath
  args = listOf("${rootProject.name}-dark", rootProject.name, getVersion())
}

val switchBackend by tasks.registering(JavaExec::class) {
  main = "${packageBase()}.SwitchDeploymentsKt"
  classpath = sourceSets["devops"].runtimeClasspath
  args = listOf(rootProject.name, "${rootProject.name}-dark")
}

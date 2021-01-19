import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.4.21"
  application
  id("com.github.johnrengelman.shadow") version "4.0.4"
}

application {
  mainClassName = "com.painkiller.ktor_starter.ApplicationKt"
}

group = "com.painkiller"
val major = 1
val minor = 0

repositories {
  jcenter()
  mavenCentral()
  maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
  maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
  implementation("io.ktor:ktor-html-builder:1.4.0")
  implementation("io.ktor:ktor-jackson:1.4.0")
  implementation("io.ktor:ktor-server-netty:1.4.0")
  implementation("org.slf4j:slf4j-simple:+")
  testImplementation("io.ktor:ktor-client-apache:1.4.0")
  testImplementation("io.ktor:ktor-client-core:1.4.0")
  testImplementation("io.ktor:ktor-client-jackson:1.4.0")
  testImplementation("io.ktor:ktor-server-test-host:1.4.0")
  testImplementation("io.mockk:mockk:+")
  testImplementation("org.hamcrest:hamcrest-junit:+")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
  testImplementation(kotlin("test-junit5"))
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
  useJUnitPlatform()
  if (System.getProperty("baseUrl") != null || System.getProperty("bspec") != null) {
    include("**/bspec/")
  }
}

tasks.withType<KotlinCompile>() {
  kotlinOptions.jvmTarget = "11"
}

tasks.named("processResources") {
  doFirst {
    ProcessBuilder("sh", "-c", "git rev-parse HEAD ; git rev-list --count HEAD")
      .start()
      .run {
        val (sha, patch) = inputStream.bufferedReader().readText().split("\n")
        file("src/main/resources/version.properties").writeText("sha=$sha\nversion=v$major.$minor.$patch")
      }
  }
}

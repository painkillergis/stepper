package com.painkiller.recommendations

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class StartApplication : BeforeAllCallback, AfterAllCallback {
  lateinit var server: NettyApplicationEngine

  override fun beforeAll(context: ExtensionContext?) {
    server = embeddedServer(
      Netty,
      applicationEngineEnvironment {
        module {
          applicationModule()
        }
        connector {
          port = 8080
        }
      },
    ).start()
  }

  override fun afterAll(context: ExtensionContext?) {
    server.stop(1000, 10000)
  }
}
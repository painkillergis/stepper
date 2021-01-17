package com.painkiller.recommendations

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@Extensions(
  ExtendWith(StartApplication::class),
  ExtendWith(TestHttpClientProvider::class),
)
class VersionBSpec {

  @TestHttpClient
  lateinit var httpClient : HttpClient

  data class Version(val version : String)

  @Test
  fun `get _version`() {
    val version = runBlocking { httpClient.get<Version>("/version") }

    assertEquals(Version("0.0.0"), version)
  }
}
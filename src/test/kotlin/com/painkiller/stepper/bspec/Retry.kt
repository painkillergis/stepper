package com.painkiller.stepper.bspec

import kotlinx.coroutines.delay

suspend fun <T> retry(times: Int, block: suspend () -> T): T {
  var delay = 250L
  repeat(times - 1) {
    try {
      return block()
    } catch (error: Error) {
      delay(delay)
      delay *= 2
    }
  }
  return block()
}
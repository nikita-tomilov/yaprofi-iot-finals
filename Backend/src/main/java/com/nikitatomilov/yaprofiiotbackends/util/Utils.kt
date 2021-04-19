package com.nikitatomilov.yaprofiiotbackends.util

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Utils {
  fun delay(millis: Int) {
    try {
      Thread.sleep(millis.toLong())
    } catch (ex: Exception) {
      //nothing
    }
  }

  fun floatFromBytes(data: ByteArray?): Float {
    return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).float
  }
}
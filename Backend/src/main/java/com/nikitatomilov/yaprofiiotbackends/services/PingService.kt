package com.nikitatomilov.yaprofiiotbackends.services

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class PingService {

  private val pings = ConcurrentHashMap<Int, Long>()

  fun registerPing(deviceId: Int, ts: Long) {
    pings[deviceId] = ts
  }

  fun latestPingTs(deviceId: Int): Long {
    return pings[deviceId] ?: 0L
  }
}
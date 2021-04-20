package com.nikitatomilov.yaprofiiotbackends.services

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.ALARM_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechdevices.HelicopterDeviceRightech
import com.nikitatomilov.yaprofiiotbackends.util.Utils.delay
import mu.KLogging
import java.util.concurrent.Executors

class HelicopterDevice(
  private val udpGateway: UDPGateway,
  private val pingService: PingService,
  private val parent: HelicopterDeviceRightech
) {

  private val executor = Executors.newFixedThreadPool(
      3,
      ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("heli-monitor-%d")
          .build())

  fun setupScheduler() {
    executor.submit {
      while (true) {
        if ((System.currentTimeMillis() - pingService.latestPingTs(DEVICE_ID)) > 6000) {
          parent.disconnectMqtt()
        } else {
          parent.tryReconnectMqtt()
        }
      }
    }
    executor.submit {
      while (true) {
        parent.emulateChangeCoordinates()
        parent.sendData()
        delay(1000)
      }
    }
  }

  fun buzzerOn() {
    sendAlarmMsg(1)
  }

  fun buzzerOff() {
    sendAlarmMsg(0)
  }

  private fun sendAlarmMsg(value: Int) {
    executor.submit {
      val msg = Message(DEVICE_ID, ALARM_RQ, 0, byteArrayOf(value.toByte(), 0, 0, 0, 0, 0, 0, 0))
      udpGateway.sendMessage(msg)
      val response = udpGateway.retrieveMessageBlocking(DEVICE_ID)
      udpGateway.clearMessage(DEVICE_ID)
    }
  }

  companion object : KLogging() {
    const val DEVICE_ID = 2
  }
}
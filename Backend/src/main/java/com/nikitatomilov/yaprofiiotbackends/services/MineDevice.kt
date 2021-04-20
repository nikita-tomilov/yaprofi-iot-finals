package com.nikitatomilov.yaprofiiotbackends.services

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.SET_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechdevices.MineDeviceRightech
import com.nikitatomilov.yaprofiiotbackends.util.Utils.delay
import mu.KLogging
import java.util.concurrent.Executors

class MineDevice(
  private val udpGateway: UDPGateway,
  private val pingService: PingService,
  private val parent: MineDeviceRightech
) {

  private val executor = Executors.newFixedThreadPool(
      3,
      ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("mine-monitor-%d")
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
      while(true) {
        parent.sendData()
        delay(1000)
      }
    }
  }

  fun buzzerOn() {
    changePinState(2, 1);
  }

  fun buzzerOff() {
    changePinState(2, 0);
  }

  fun ventOn() {
    changePinState(3, 1);
  }

  fun ventOff() {
    changePinState(3, 0);
  }

  private fun changePinState(pin: Int, value: Int) {
    executor.submit {
      val msg = Message(DEVICE_ID, SET_RQ, pin, byteArrayOf(value.toByte(), 0, 0, 0, 0, 0, 0, 0))
      udpGateway.sendMessage(msg)
      val response = udpGateway.retrieveMessageBlocking(DEVICE_ID)
      udpGateway.clearMessage(DEVICE_ID)
    }
  }

  companion object : KLogging() {
    const val DEVICE_ID = 1
  }
}
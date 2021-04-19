package com.nikitatomilov.yaprofiiotbackends.services

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.SET_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechdevices.SuitDeviceRightech
import mu.KLogging
import java.util.concurrent.Executors

class SuitDevice(
  private val udpGateway: UDPGateway,
  private val pingService: PingService,
  private val parent: SuitDeviceRightech
) {

  private val executor = Executors.newFixedThreadPool(2,
      ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("suit-monitor")
          .build())

  fun activeLedOn() {
    changePinState(8, 1);
  }

  fun activeLedOff() {
    changePinState(8, 0);
  }

  fun setupScheduler() {
    executor.submit {
      while (true) {
        try {
          val msg = udpGateway.retrieveMessageBlocking(DEVICE_ID)
          if (msg.type == Message.GET_RP) {
            val oxygen = (msg.payload[0].asUint() / 255.0 * 100.0).toInt()
            val battery = (msg.payload[1].asUint() / 255.0 * 100.0).toInt()
            parent.sendData(oxygen, battery)
            var flag = 0
            if (msg.payload[2].toInt() == 1) flag = 1
            if (msg.payload[3].toInt() == 1) flag = 2
            if (msg.payload[4].toInt() == 1) flag = 3
            if (msg.payload[5].toInt() == 1) flag = 4
            parent.changePosition(flag)
          }
          udpGateway.clearMessage(DEVICE_ID)
        } catch (e: Exception) {
        }
        if ((System.currentTimeMillis() - pingService.latestPingTs(DEVICE_ID)) > 6000) {
          parent.disconnectMqtt()
        } else {
          parent.tryReconnectMqtt()
        }
      }
    }
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
    private const val DEVICE_ID = 3
  }

  private fun Byte.asUint() = this.toUByte().toInt()
}
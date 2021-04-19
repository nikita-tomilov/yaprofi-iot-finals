package com.nikitatomilov.yaprofiiotbackends.devices

import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.ALARM_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.SET_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class HelicopterDevice(
  private val udpGateway: UDPGateway
) {

  fun buzzerOn() {
    sendAlarmMsg(1)
  }

  fun buzzerOff() {
    sendAlarmMsg(0)
  }

  private fun sendAlarmMsg(value: Int) {
    val msg = Message(DEVICE_ID, ALARM_RQ, 0, byteArrayOf(value.toByte(), 0, 0, 0, 0, 0, 0, 0))
    udpGateway.sendMessage(msg)
    val response = udpGateway.retrieveMessageBlocking(DEVICE_ID)
    udpGateway.clearMessage(DEVICE_ID)
  }

  companion object : KLogging() {
    private const val DEVICE_ID = 2
  }
}
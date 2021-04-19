package com.nikitatomilov.yaprofiiotbackends.devices

import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.SET_RQ
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class MineDevice(
  private val udpGateway: UDPGateway
) {

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
    val msg = Message(DEVICE_ID, SET_RQ, pin, byteArrayOf(value.toByte(), 0, 0, 0, 0, 0, 0, 0))
    udpGateway.sendMessage(msg)
    val response = udpGateway.retrieveMessageBlocking(DEVICE_ID)
    udpGateway.clearMessage(DEVICE_ID)
  }

  companion object : KLogging() {
    private const val DEVICE_ID = 1;
  }
}
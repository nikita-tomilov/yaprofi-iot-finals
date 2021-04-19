package com.nikitatomilov.yaprofiiotbackends.rightechdevices

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import com.nikitatomilov.yaprofiiotbackends.services.PingService
import mu.KLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SuitDeviceRightech(
  @Value("\${rightechMqttAddress:tcp://sandbox.rightech.io:1883}") private val mqttUrl: String,
  private val udpGateway: UDPGateway,
  private val pingService: PingService
) {

  private lateinit var obj: Object

  private lateinit var mqttClient: MqttClient

  private val connected = AtomicBoolean(false)

  private val executor = Executors.newSingleThreadExecutor(
      ThreadFactoryBuilder()
          .setDaemon(true)
          .setNameFormat("suit-monitor")
          .build())

  fun setup(obj: Object) {
    logger.warn { "Going to connect to $mqttUrl" }
    this.obj = obj
    connectMqtt()
    setupScheduler()
  }

  private fun connectMqtt() {
    mqttClient = MqttClient(mqttUrl, obj.id)
    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true
    options.isCleanSession = true
    options.connectionTimeout = 10
    mqttClient.connect(options)
    connected.set(true)
    sendInitialData()
    logger.warn { "Suit went online" }
  }

  private fun setupScheduler() {
    executor.submit {
      while (true) {
        try {
          val msg = udpGateway.retrieveMessageBlocking(SUIT_NODE_ID)
          if (msg.type == Message.GET_RP) {
            val oxygen = (msg.payload[0].asUint() / 255.0 * 100.0).toInt()
            val battery = (msg.payload[1].asUint() / 255.0 * 100.0).toInt()
            sendData(oxygen, battery)
          }
          udpGateway.clearMessage(SUIT_NODE_ID)
        } catch (e: Exception) {
        }
        if ((System.currentTimeMillis() - pingService.latestPingTs(SUIT_NODE_ID)) > 6000) {
          disconnectMqtt()
        } else {
          tryReconnectMqtt()
        }
      }
    }
  }

  fun disconnectMqtt() {
    if (!connected.get()) return
    mqttClient.disconnect()
    connected.set(false)
    logger.warn { "Suit went offline" }
  }

  fun tryReconnectMqtt() {
    if (connected.get()) return
    connectMqtt()
  }

  private fun sendInitialData() {
    if (connected.get()) {
      mqttClient.publish("/state/charge", "100".toByteArray(), 2, true)
      mqttClient.publish("/state/active", "true".toByteArray(), 2, true)
      mqttClient.publish("environment/oxygen", "80".toByteArray(), 2, true)
      mqttClient.publish("environment/humidity", "100".toByteArray(), 2, true)
      mqttClient.publish("environment/carbon", "0".toByteArray(), 2, true)
      mqttClient.publish("environment/nitric", "0".toByteArray(), 2, true)
      mqttClient.publish("environment/sulfurous", "0".toByteArray(), 2, true)
      mqttClient.publish("environment/hydrogen_sulfide", "0".toByteArray(), 2, true)
      mqttClient.publish("environment/methane", "0".toByteArray(), 2, true)
      mqttClient.publish("environment/dust", "0".toByteArray(), 2, true)
    }
  }

  private fun sendData(oxygenLevel: Int, batteryCharge: Int) {
    if (connected.get()) {
      mqttClient.publish("/state/charge", "$batteryCharge".toByteArray(), 2, true)
      mqttClient.publish("environment/oxygen", "$oxygenLevel".toByteArray(), 2, true)
    }
  }

  private fun Byte.asUint() = this.toUByte().toInt()

  companion object : KLogging() {
    private const val SUIT_NODE_ID = 3
  }
}
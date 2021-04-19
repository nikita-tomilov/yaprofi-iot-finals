package com.nikitatomilov.yaprofiiotbackends.rightechdevices

import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import mu.KLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SuitDeviceRightech(
  @Value("\${rightechMqttAddress:tcp://sandbox.rightech.io:1883}") private val mqttUrl: String
) {

  private lateinit var obj: Object

  private lateinit var mqttClient: MqttClient

  private val connected = AtomicBoolean(false)

  fun setup(obj: Object) {
    logger.warn { "Going to connect to $mqttUrl" }
    this.obj = obj
    connectMqtt()
  }

  fun connectMqtt() {
    mqttClient = MqttClient(mqttUrl, obj.id)
    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true
    options.isCleanSession = true
    options.connectionTimeout = 10
    mqttClient.connect(options)
    connected.set(true)
    sendInitialData()
  }

  fun disconnectMqtt() {
    mqttClient.disconnectForcibly()
    connected.set(false)
  }

  fun sendInitialData() {
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

  fun sendData(oxygenLevel: Int, batteryCharge: Int) {
    if (connected.get()) {
      mqttClient.publish("/state/charge", "$batteryCharge".toByteArray(), 2, true)
      mqttClient.publish("environment/oxygen", "$oxygenLevel".toByteArray(), 2, true)
    }
  }

  companion object : KLogging()
}
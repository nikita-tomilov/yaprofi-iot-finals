package com.nikitatomilov.yaprofiiotbackends.rightechdevices

import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import com.nikitatomilov.yaprofiiotbackends.services.MineDevice
import com.nikitatomilov.yaprofiiotbackends.services.PingService
import mu.KLogging
import org.eclipse.paho.client.mqttv3.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class MineDeviceRightech(
  @Value("\${rightechMqttAddress:tcp://sandbox.rightech.io:1883}") private val mqttUrl: String,
  private val udpGateway: UDPGateway,
  private val pingService: PingService
) {

  private lateinit var obj: Object

  private lateinit var mqttClient: MqttClient

  private lateinit var mineDevice: MineDevice

  private val connected = AtomicBoolean(false)

  @Volatile
  private var ventActive = false

  @Volatile
  private var buzzerActive = false

  fun setup(obj: Object) {
    logger.warn { "Going to connect to $mqttUrl" }
    this.obj = obj
    connectMqtt()
    mineDevice = MineDevice(udpGateway, pingService, this)
    mineDevice.setupScheduler()
  }

  private fun connectMqtt() {
    mqttClient = MqttClient(mqttUrl, obj.id)
    val options = MqttConnectOptions()
    options.isAutomaticReconnect = true
    options.isCleanSession = true
    options.connectionTimeout = 10
    mqttClient.connect(options)
    mqttClient.setCallback(object : MqttCallback {
      override fun messageArrived(p0: String, p1: MqttMessage) {
        mqttCallback(p0, p1)
      }

      override fun connectionLost(p0: Throwable?) {
        //for olympiad - nothing, but we should think about it for actual production usage
      }

      override fun deliveryComplete(p0: IMqttDeliveryToken?) {
        //nothing
      }
    })
    connected.set(true)
    sendInitialData()
    logger.warn { "Suit went online" }
  }

  fun disconnectMqtt() {
    if (!connected.get()) return
    mqttClient.disconnect()
    connected.set(false)
    logger.warn { "Mineshaft went offline" }
  }

  fun tryReconnectMqtt() {
    if (connected.get()) return
    connectMqtt()
  }

  private fun sendInitialData() {
    if (connected.get()) {
      //      mqttClient.publish("/state/charge", "100".toByteArray(), 2, true)
      //      mqttClient.publish("/state/is_active", "false".toByteArray(), 2, true)
      //      mqttClient.publish("environment/oxygen", "80".toByteArray(), 2, true)
      //      mqttClient.publish("environment/humidity", "100".toByteArray(), 2, true)
      //      mqttClient.publish("environment/carbon", "0".toByteArray(), 2, true)
      //      mqttClient.publish("environment/nitric", "0".toByteArray(), 2, true)
      //      mqttClient.publish("environment/sulfurous", "0".toByteArray(), 2, true)
      //      mqttClient.publish("environment/hydrogen_sulfide", "0".toByteArray(), 2, true)
      //      mqttClient.publish("environment/methane", "0".toByteArray(), 2, true)
      //      mqttClient.publish("environment/dust", "0".toByteArray(), 2, true)
    }
  }

  fun sendData() {
    if (connected.get()) {
      mqttClient.publish("ventilation_active", "$ventActive".toByteArray(), 2, true)
      mqttClient.publish("buzzer_active", "$buzzerActive".toByteArray(), 2, true)
    }
  }

  private fun mqttCallback(topic: String, msg: MqttMessage) {
    logger.warn { "Incoming from topic $topic: $msg" }
    if (topic == "buzzer") {
      val active = (String(msg.payload) == "true")
      //there is some weird concurrency going on, gotta be careful
      if (active) {
        mineDevice.buzzerOn()
        buzzerActive = true
      } else {
        mineDevice.buzzerOff()
        buzzerActive = false
      }
    }
    if (topic == "ventilation") {
      val active = (String(msg.payload) == "true")
      //there is some weird concurrency going on, gotta be careful
      if (active) {
        mineDevice.ventOn()
        ventActive = true
      } else {
        mineDevice.ventOff()
        ventActive = false
      }
    }
  }

  companion object : KLogging()
}
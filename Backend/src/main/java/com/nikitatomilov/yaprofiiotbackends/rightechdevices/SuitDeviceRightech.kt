package com.nikitatomilov.yaprofiiotbackends.rightechdevices

import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.mqtt.BleDevice
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.mqtt.CoordsPackage
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.mqtt.CoordsPackage.Companion.listOfEmpty
import com.nikitatomilov.yaprofiiotbackends.services.PingService
import com.nikitatomilov.yaprofiiotbackends.services.SuitDevice
import mu.KLogging
import org.eclipse.paho.client.mqttv3.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SuitDeviceRightech(
  @Value("\${rightechMqttAddress:tcp://sandbox.rightech.io:1883}") private val mqttUrl: String,
  private val udpGateway: UDPGateway,
  private val pingService: PingService
) {

  private lateinit var obj: Object

  private lateinit var mqttClient: MqttClient

  private lateinit var suitDevice: SuitDevice

  private val connected = AtomicBoolean(false)
  @Volatile
  private var suitActive = false
  @Volatile
  private var coordsPackage = COORDS_OUTSIDE
  @Volatile
  private var prevPosition = 1

  fun setup(obj: Object) {
    logger.warn { "Going to connect to $mqttUrl" }
    this.obj = obj
    connectMqtt()
    suitDevice = SuitDevice(udpGateway, pingService, this)
    suitDevice.setupScheduler()
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
    logger.warn { "Suit went offline" }
  }

  fun tryReconnectMqtt() {
    if (connected.get()) return
    connectMqtt()
  }

  private fun sendInitialData() {
    if (connected.get()) {
      mqttClient.publish("/state/charge", "100".toByteArray(), 2, true)
      mqttClient.publish("/state/is_active", "false".toByteArray(), 2, true)
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
      mqttClient.publish("/state/is_active", "$suitActive".toByteArray(), 2, true)
      mqttClient.publish("/state/charge", "$batteryCharge".toByteArray(), 2, true)
      mqttClient.publish("environment/oxygen", "$oxygenLevel".toByteArray(), 2, true)
    }
  }

  fun changePosition(positionFlag: Int) {
    if (connected.get() && (prevPosition != positionFlag)) {
      prevPosition = positionFlag
      coordsPackage = when(positionFlag) {
        1 -> COORDS_OUTSIDE
        2 -> COORDS_INSIDE
        3 -> COORDS_DANGER_1
        4 -> COORDS_DANGER_2
        else -> COORDS_OUTSIDE
      }
      mqttClient.publish("beacons", coordsPackage.toBase64().toByteArray(), 2, true)
    }
  }

  private fun mqttCallback(topic: String, msg: MqttMessage) {
    logger.warn { "Incoming from topic $topic: $msg" }
    if (topic == "/state/active") {
      val active = (String(msg.payload) == "true")
      //there is some weird concurrency going on, gotta be careful
      if (active) {
        suitDevice.activeLedOn()
        suitActive = true
      } else {
        suitDevice.activeLedOff()
        suitActive = false
      }
    }
  }

  companion object : KLogging() {
    private val COORDS_OUTSIDE = CoordsPackage(67.562879f, 63.774948f, listOfEmpty())
    private val COORDS_INSIDE = CoordsPackage(67.56419f, 63.847046f, listOf(
        BleDevice(0x9812, -64),
        BleDevice(0x0a35, -63),
        BleDevice(0x2939, -62),
        BleDevice(0xd396, -61),
        BleDevice(0xf741, -60),
        BleDevice(0x01dd, -59),
        BleDevice(0x08cd, -58),
        BleDevice(0x0e60, -57)
    ))
    private val COORDS_DANGER_1 = CoordsPackage(67.597448f, 63.707657f, listOfEmpty())
    private val COORDS_DANGER_2 = CoordsPackage(67.63092f, 64.109344f, listOfEmpty())
  }
}
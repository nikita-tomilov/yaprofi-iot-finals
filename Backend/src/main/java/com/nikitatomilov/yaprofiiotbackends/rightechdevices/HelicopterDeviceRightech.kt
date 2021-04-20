package com.nikitatomilov.yaprofiiotbackends.rightechdevices

import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import com.nikitatomilov.yaprofiiotbackends.services.HelicopterDevice
import com.nikitatomilov.yaprofiiotbackends.services.PingService
import mu.KLogging
import org.eclipse.paho.client.mqttv3.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class HelicopterDeviceRightech(
  @Value("\${rightechMqttAddress:tcp://sandbox.rightech.io:1883}") private val mqttUrl: String,
  private val udpGateway: UDPGateway,
  private val pingService: PingService
) {

  private lateinit var obj: Object

  private lateinit var mqttClient: MqttClient

  private lateinit var helicopterDevice: HelicopterDevice

  private val connected = AtomicBoolean(false)

  private var alarmActive = false
  private var adc = 5000
  private var lat = 67.619419f
  private var lon = 63.875885f
  private var latDelta = 0.001f
  private var lonDelta = 0.001f

  fun setup(obj: Object) {
    logger.warn { "Going to connect to $mqttUrl" }
    this.obj = obj
    connectMqtt()
    helicopterDevice = HelicopterDevice(udpGateway, pingService, this)
    helicopterDevice.setupScheduler()
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
    logger.warn { "Helicopter went online" }
  }

  fun disconnectMqtt() {
    if (!connected.get()) return
    mqttClient.disconnect()
    connected.set(false)
    logger.warn { "Helicopter went offline" }
  }

  fun tryReconnectMqtt() {
    if (connected.get()) return
    connectMqtt()
  }

  private fun sendInitialData() {
    if (connected.get()) {
      sendData()
    }
  }

  fun sendData() {
    if (connected.get()) {
      mqttClient.publish("buzzer_active", "$alarmActive".toByteArray(), 2, true)
      mqttClient.publish("adc", "$adc".toByteArray(), 2, true)
      mqttClient.publish("base/state/pos", """{"lat": $lat, "lon": $lon}""".toByteArray(), 2, true)
    }
  }

  fun emulateChangeCoordinates() {
    if (connected.get()) {
      lat += latDelta
      lon += lonDelta
      if (lat > maxLat) { latDelta *= -1; lat = maxLat; }
      if (lat < minLat) { latDelta *= -1; lat = minLat; }
      if (lon > maxLon) { lonDelta *= -1; lat = maxLon; }
      if (lon < minLon) { lonDelta *= -1; lat = minLon; }
      adc -= 1
      if (adc < 1000) adc = 5000
    }
  }

  private fun mqttCallback(topic: String, msg: MqttMessage) {
    logger.warn { "Incoming from topic $topic: $msg" }
    if (topic == "buzzer") {
      val active = (String(msg.payload) == "true")
      //there is some weird concurrency going on, gotta be careful
      if (active) {
        helicopterDevice.buzzerOn()
        alarmActive = true
      } else {
        helicopterDevice.buzzerOff()
        alarmActive = false
      }
    }
  }

  companion object : KLogging() {
    private const val minLat = 67.6f
    private const val maxLat = 67.63f

    private const val minLon = 63.87f
    private const val maxLon = 63.88f
  }
}
package com.nikitatomilov.yaprofiiotbackends.rightechintegration

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.nikitatomilov.yaprofiiotbackends.communication.Message.Companion.GET_RP
import com.nikitatomilov.yaprofiiotbackends.communication.UDPGateway
import com.nikitatomilov.yaprofiiotbackends.rightechdevices.SuitDeviceRightech
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ModelApi
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ObjectApi
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.util.RightechFeignRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Service
class RightechIntegrationService(
  @Value("\${rightechAddress:https://sandbox.rightech.io/}") private val url: String,
  @Value("#{environment.RIGHTECH_API_TOKEN}") private val token: String,
  private val suitDeviceRightech: SuitDeviceRightech,
  private val udpGateway: UDPGateway
) {

  private lateinit var objectApi: ObjectApi
  private lateinit var modelApi: ModelApi

  private val executor = Executors.newSingleThreadExecutor(ThreadFactoryBuilder()
      .setDaemon(true)
      .setNameFormat("suit-monitor")
      .build())

  @PostConstruct
  fun setupIntegration() {
    logger.warn { "Using Rightech Address $url and token $token" }
    val fr = RightechFeignRepository(url, token)
    val objectApi = fr.buildObjectsApi()
    val modelApi = fr.buildModelsApi()

    val models = modelApi.getModels()
    logger.warn { "Found ${models.size} models" }

    val modelForSuit = models.single { it.name.contains("костюмом") }
    val modelForMine = models.single { it.name.contains("шахтой") }
    val modelForHelicopter = models.single { it.name.contains("вертолетом") }

    val objects = objectApi.getObjects()
    logger.warn { "Found ${objects.size} objects" }

    val objectForSuit =
        objects.single { (it.model == modelForSuit._id) && (it.name!!.doesNotCountain("бот")) }
    logger.warn { "Going to use $objectForSuit as Suit" }
    suitDeviceRightech.setup(objectForSuit)

    executor.submit {
      while (true) {
        try {
          val msg = udpGateway.retrieveMessageBlocking(SUIT_NODE_ID)
          if (msg.type == GET_RP) {
            val oxygen = (msg.payload[0].asUint() / 255.0 * 100.0).toInt()
            val battery = (msg.payload[1].asUint() / 255.0 * 100.0).toInt()
            suitDeviceRightech.sendData(oxygen, battery)
          }
          udpGateway.clearMessage(SUIT_NODE_ID)
        } catch (e: Exception) {}
      }
    }
  }

  companion object : KLogging() {
    private const val SUIT_NODE_ID = 3
  }

  private fun String.doesNotCountain(s: String): Boolean {
    return !this.contains(s)
  }

  private fun Byte.asUint() = this.toUByte().toInt()
}
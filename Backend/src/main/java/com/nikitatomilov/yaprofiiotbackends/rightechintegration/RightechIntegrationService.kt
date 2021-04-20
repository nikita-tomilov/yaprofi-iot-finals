package com.nikitatomilov.yaprofiiotbackends.rightechintegration

import com.nikitatomilov.yaprofiiotbackends.rightechdevices.HelicopterDeviceRightech
import com.nikitatomilov.yaprofiiotbackends.rightechdevices.SuitDeviceRightech
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ModelApi
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ObjectApi
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.util.RightechFeignRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class RightechIntegrationService(
  @Value("\${rightechAddress:https://sandbox.rightech.io/}") private val url: String,
  @Value("#{environment.RIGHTECH_API_TOKEN}") private val token: String,
  private val suitDeviceRightech: SuitDeviceRightech,
  private val helicopterDeviceRightech: HelicopterDeviceRightech
) {

  private lateinit var objectApi: ObjectApi
  private lateinit var modelApi: ModelApi

  @PostConstruct
  fun setupIntegration() {
    logger.warn { "Using Rightech Address $url and token $token" }
    val fr = RightechFeignRepository(url, token)
    objectApi = fr.buildObjectsApi()
    modelApi = fr.buildModelsApi()

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

    val objectForHeli =
        objects.single { (it.model == modelForHelicopter._id) && (it.name!!.doesNotCountain("бот")) }
    logger.warn { "Going to use $objectForHeli as Helicopter" }
    helicopterDeviceRightech.setup(objectForHeli)
  }

  private fun String.doesNotCountain(s: String): Boolean {
    return !this.contains(s)
  }

  companion object : KLogging()
}
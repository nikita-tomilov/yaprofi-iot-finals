package com.nikitatomilov.yaprofiiotbackends.rightechintegration.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ModelApi
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.api.ObjectApi
import feign.Client
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.okhttp.OkHttpClient
import mu.KLogging

class RightechFeignRepository(
  private val url: String,
  private val token: String,
  private val client: Client = OkHttpClient()
) {

  private val mapper = ObjectMapper().registerKotlinModule()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  private var requestInterceptor = RequestInterceptor {
    it.header("Content-Type", "application/json")
    it.header("Authorization", "Bearer $token")
  }

  fun buildModelsApi(): ModelApi =
      commonBuilder().requestInterceptor(requestInterceptor).target(ModelApi::class.java, url)

  fun buildObjectsApi(): ObjectApi =
      commonBuilder().requestInterceptor(requestInterceptor).target(ObjectApi::class.java, url)

  private fun commonBuilder() = Feign.builder()
      .client(client)
      .encoder(JacksonEncoder(mapper))
      .decoder(JacksonDecoder(mapper))
      .logLevel(Logger.Level.FULL)
      .logger(object : Logger() {
        override fun log(p0: String, p1: String, vararg p2: Any) {
          if (p1 == "%s") {
            logger.info { "# RawResponse: ${p2[0]}" }
          }
        }
      })

  companion object : KLogging()
}
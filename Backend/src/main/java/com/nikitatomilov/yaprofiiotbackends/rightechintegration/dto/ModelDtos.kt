package com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import java.util.*

data class Model(
  val _id: String,
  val name: String,
  val base: String,
  val description: String
) {
  private val unknownFields = HashMap<String, Any>()

  @JsonAnyGetter
  fun any(): Map<String, Any> {
    return unknownFields
  }

  @JsonAnySetter
  fun set(name: String, value: Any) {
    unknownFields[name] = value
  }
}
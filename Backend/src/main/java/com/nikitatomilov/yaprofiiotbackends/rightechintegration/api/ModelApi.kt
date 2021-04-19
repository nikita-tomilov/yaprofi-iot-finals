package com.nikitatomilov.yaprofiiotbackends.rightechintegration.api

import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Model
import feign.RequestLine

interface ModelApi {
  @RequestLine("GET /api/v1/models")
  fun getModels(): List<Model>
}
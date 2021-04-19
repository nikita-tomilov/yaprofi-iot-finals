package com.nikitatomilov.yaprofiiotbackends.rightechintegration.api

import feign.RequestLine
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.Object
import com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto.ObjectCreateRequest

interface ObjectApi {
  @RequestLine("GET /api/v1/objects")
  fun getObjects(): List<Object>

  @RequestLine("POST /api/v1/objects")
  fun createObject(rq: ObjectCreateRequest): Object
}
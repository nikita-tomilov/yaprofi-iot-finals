package com.nikitatomilov.yaprofiiotbackends.rightechintegration.dto

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import java.util.*

data class ObjectCreateRequest(
  val model: String,
  val id: String,
  val name: String
)

data class Packet(
  val _id: String, /* Идентификатор пакета */
  val _ts: Long,   /* Время сервера (микросекунды) */
  val time: Long,  /* Время регистрации пакета устройством или сервером (миллисекунды) */
  val online: Boolean,
  val lat: Number?,     /* Широта */
  val lon: Number?,     /* Долгота */
  val height: Number?,  /* Высота над уровнем моря в метрах */
  val angle: Number?,   /* Угол поворота в градусах */
  val speed: Number?    /* Мгновенная cкорость в км/ч */
)

data class Object(
  val _id: String?,     /* Внутренний идентификатор в системе */
  val id: String?,      /* Идентификатор объекта, присваиваемый пользователем  */
  val name: String?,
  val model: String?,   /* Идентификатор модели объекта контроля */
  val group: String?,
  val status: String?,  /* Текстовый статус объекта */
  val active: Boolean?,
  val state: Packet? /* Последний пакет данных */
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
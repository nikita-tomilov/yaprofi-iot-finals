package com.nikitatomilov.yaprofiiotbackends.communication

import java.time.Instant

data class Message(
  val nodeID: Int,
  val type: Int,
  val regID: Int,
  val payload: ByteArray,
  val timestamp: Long = Instant.now().toEpochMilli()
) {
  fun toBytes(): ByteArray {
    val ans = ByteArray(11)
    ans[0] = nodeID.toByte()
    ans[1] = type.toByte()
    ans[2] = regID.toByte()
    for (i in payload.indices) {
      ans[3 + i] = payload[i]
    }
    return ans
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Message

    if (nodeID != other.nodeID) return false
    if (type != other.type) return false
    if (regID != other.regID) return false
    if (!payload.contentEquals(other.payload)) return false
    if (timestamp != other.timestamp) return false

    return true
  }

  override fun hashCode(): Int {
    var result = nodeID
    result = 31 * result + type
    result = 31 * result + regID
    result = 31 * result + payload.contentHashCode()
    result = 31 * result + timestamp.hashCode()
    return result
  }

  companion object {
    fun fromBytes(data: ByteArray): Message {
      return Message(
          data[0].toInt(),
          data[1].toInt(),
          data[2].toInt(),
          data.sliceArray(3 until 11))
    }

    const val GET_RQ = 0
    const val SET_RQ = 1
    const val GET_RP = 2
    const val SET_RP = 3
    const val PING = 4
    const val PONG = 5
    const val ALARM_RQ = 6
    const val ALARM_RP = 7

    const val DIGITAL_WRITE = 0
    const val DIGITAL_READ = 1
    const val ANALOG_WRITE = 2
    const val ANALOG_READ = 3
    const val RELAY_ON = 4
    const val RELAY_OFF = 5
  }
}
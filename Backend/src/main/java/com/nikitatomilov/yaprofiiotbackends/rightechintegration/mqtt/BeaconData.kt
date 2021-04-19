package com.nikitatomilov.yaprofiiotbackends.rightechintegration.mqtt

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList

data class BleDevice(
  val mac: Short,
  val rssi: Byte
) {

  constructor(mac: Int, rssi: Int): this(mac.toShort(), rssi.toByte())

  companion object {
    fun parse(source: ByteArray, order: ByteOrder): BleDevice {
      val buffer = ByteBuffer.wrap(source)
      buffer.order(order)
      val mac = buffer.getShort()
      val rssi = buffer.get()
      return BleDevice(mac, rssi)
    }
  }

  fun toBytes(order: ByteOrder): ByteArray {
    val buf = ByteBuffer.allocate(3)
    buf.order(order)
    buf.putShort(mac)
    buf.put(rssi)
    return buf.array()
  }

  override fun toString(): String {
    return "${Integer.toHexString(mac.toInt()).replace("f", "").padStart(4, '0')}: $rssi"
  }
}

data class SuitPacket(
  val gpsLong: Float,
  val gpsLat: Float,
  val gpsAlt: Float,
  val timestamp: Int,
  val seenDevices: Byte,
  val records: List<BleDevice>
) {
  companion object {
    fun parse(source: ByteArray, order: ByteOrder): SuitPacket {
      val buffer = ByteBuffer.wrap(source)
      buffer.order(order)

      val lo = buffer.getFloat()
      val la = buffer.getFloat()
      val alt = buffer.getFloat()
      val ts = buffer.getInt()
      val seen = buffer.get()

      val ps = ArrayList<BleDevice>()
      (0 until seen).forEach {
        val bbb = ByteArray(3)
        (0 until 3).forEach {
          bbb[it] = buffer.get()
        }
        ps.add(BleDevice.parse(bbb, order))
      }

      return SuitPacket(lo, la, alt, ts, seen, ps)
    }
  }

  fun toBytes(order: ByteOrder): ByteArray {
    val buf = ByteBuffer.allocate(4 + 4 + 4 + 4 + 1 + records.size * 3)
    buf.order(order)
    buf.putFloat(gpsLong)
    buf.putFloat(gpsLat)
    buf.putFloat(gpsAlt)
    buf.putInt(timestamp)
    buf.put(seenDevices)
    records.forEach { buf.put(it.toBytes(order)) }
    return buf.array()
  }
}

data class CoordsPackage(
  val lat: Float,
  val lon: Float,
  val beacons: List<BleDevice>
) {

  fun toBase64(): String {
    val packet = SuitPacket(
        lat,
        lon,
        0.0f,
        (System.currentTimeMillis() / 1000).toInt(),
        beacons.size.toByte(),
        beacons
    )
    val bytes = packet.toBytes(ByteOrder.LITTLE_ENDIAN)
    return Base64.getEncoder().encodeToString(bytes)
  }

  companion object {
    fun listOfEmpty() = listOf(
        BleDevice(0x9812, -127),
        BleDevice(0x0a35, -127),
        BleDevice(0x2939, -127),
        BleDevice(0xd396, -127),
        BleDevice(0xf741, -127),
        BleDevice(0x01dd, -127),
        BleDevice(0x08cd, -127),
        BleDevice(0x0e60, -127)
    )
  }
}
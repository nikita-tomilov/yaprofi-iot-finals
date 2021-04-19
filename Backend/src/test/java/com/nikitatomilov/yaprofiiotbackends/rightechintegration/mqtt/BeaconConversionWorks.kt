package com.nikitatomilov.yaprofiiotbackends.rightechintegration.mqtt

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.ByteOrder
import java.util.*
import javax.xml.bind.DatatypeConverter

class BeaconConversionWorks {

  @Test
  fun itWorks() {
    //given
    val orig = Base64.getDecoder().decode("CtSLQu35ckLNO4Y/vpUOYAQSmIE1CoE5KYGW04E=")
    val origPacket = SuitPacket.parse(orig, ByteOrder.LITTLE_ENDIAN)
    //when
    val converted = origPacket.toBytes(ByteOrder.LITTLE_ENDIAN)
    //then
    assertThat(converted.contentEquals(orig))
  }

  private fun String.fromHexStringToBytes(): ByteArray {
    return DatatypeConverter.parseHexBinary(this.replace(" ", ""))
  }
}
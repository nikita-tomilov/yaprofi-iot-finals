package com.nikitatomilov.yaprofiiotbackends.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {

  public static void delay(int millis) {
    try {
      Thread.sleep(millis);
    } catch (Exception ex) {
      //nothing
    }
  }

  public static float floatFromBytes(byte[] data) {
    return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getFloat();
  }
}

package com.hamilton.common;

import java.util.logging.Logger;

public final class Util {
  private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

  public static void sleepSeconds(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      LOGGER.warning("Sleep interrupted: " + e.getMessage());
    }
  }

  public static void sleepSeconds(double seconds) {
    long millis = (long)(seconds * 1000.0);
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      LOGGER.warning("Sleep interrupted: " + e.getMessage());
    }
  }

  /**
   * loopForever is suitable for use in loops that intend to loop forever, instead of using while (true). This avoids
   * killing the RPC server in KSP by only looping once every 100 milliseconds.
   */
  public static boolean loopForever() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      LOGGER.warning("Sleep interrupted: " + e.getMessage());
    }

    return true;
  }
}

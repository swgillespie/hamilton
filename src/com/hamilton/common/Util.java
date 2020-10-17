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
}

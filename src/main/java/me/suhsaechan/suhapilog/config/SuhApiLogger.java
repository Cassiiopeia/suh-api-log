package me.suhsaechan.suhapilog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuhApiLogger {
  private final Logger log;
  private static final String PREFIX = "[SUH-API-LOG] : ";

  private SuhApiLogger(Class<?> clazz) {
    this.log = LoggerFactory.getLogger(clazz);
  }

  public static SuhApiLogger getLogger(Class<?> clazz) {
    return new SuhApiLogger(clazz);
  }

  public void debug(String message, Object... args) {
    if (log.isDebugEnabled()) {
      log.debug(PREFIX + message, args);
    }
  }

  public void info(String message, Object... args) {
    log.info(PREFIX + message, args);
  }

  public void warn(String message, Object... args) {
    log.warn(PREFIX + message, args);
  }

  public void error(String message, Object... args) {
    log.error(PREFIX + message, args);
  }

  public void error(String message, Throwable t) {
    log.error(PREFIX + message, t);
  }
}

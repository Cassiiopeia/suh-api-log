package kr.suhsaechan.suhapilog.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 2순위 어댑터. SLF4J 없이 Log4j2 API만 존재하는 환경에서 선택된다.
 * Log4j2도 {} 플레이스홀더를 네이티브 지원하므로 포맷팅을 위임한다.
 */
final class Log4j2LoggerAdapter implements SuhLogger {

  private final Logger logger;

  Log4j2LoggerAdapter(String name) {
    this.logger = LogManager.getLogger(name);
  }

  @Override public void trace(String message) { logger.trace(message); }

  @Override public void trace(String message, Object... args) { logger.trace(message, args); }

  @Override public void debug(String message) { logger.debug(message); }

  @Override public void debug(String message, Object... args) { logger.debug(message, args); }

  @Override public void info(String message) { logger.info(message); }

  @Override public void info(String message, Object... args) { logger.info(message, args); }

  @Override public void warn(String message) { logger.warn(message); }

  @Override public void warn(String message, Object... args) { logger.warn(message, args); }

  @Override public void error(String message) { logger.error(message); }

  @Override public void error(String message, Object... args) { logger.error(message, args); }

  @Override
  public void error(String message, Throwable throwable) {
    logger.error(message, throwable);
  }

  @Override public boolean isTraceEnabled() { return logger.isTraceEnabled(); }

  @Override public boolean isDebugEnabled() { return logger.isDebugEnabled(); }

  @Override public boolean isInfoEnabled() { return logger.isInfoEnabled(); }

  @Override public boolean isWarnEnabled() { return logger.isWarnEnabled(); }

  @Override public boolean isErrorEnabled() { return logger.isErrorEnabled(); }
}

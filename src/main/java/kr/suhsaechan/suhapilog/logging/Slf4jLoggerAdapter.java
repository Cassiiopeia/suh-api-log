package kr.suhsaechan.suhapilog.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1순위 어댑터. 상위 프로젝트가 log4j2를 쓰든 logback을 쓰든 SLF4J 바인딩을 통해
 * 그 설정이 그대로 적용된다.
 *
 * <p>{} 포맷팅을 SLF4J에 위임하므로 레벨이 비활성일 때 포맷팅 비용이 발생하지 않는다.
 */
final class Slf4jLoggerAdapter implements SuhLogger {

  private final Logger logger;

  Slf4jLoggerAdapter(String name) {
    this.logger = LoggerFactory.getLogger(name);
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

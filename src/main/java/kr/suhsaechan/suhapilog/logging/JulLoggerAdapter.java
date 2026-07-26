package kr.suhsaechan.suhapilog.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 최종 폴백 어댑터. SLF4J도 Log4j2도 없는 환경에서만 선택된다.
 *
 * <p>JUL은 {} 플레이스홀더를 모르므로 이 경로에서만 자체 포맷터를 쓴다.
 */
final class JulLoggerAdapter implements SuhLogger {

  private final Logger logger;

  JulLoggerAdapter(String name) {
    this.logger = Logger.getLogger(name);
  }

  @Override
  public void trace(String message) {
    logger.log(Level.FINER, message);
  }

  @Override
  public void trace(String message, Object... args) {
    if (logger.isLoggable(Level.FINER)) {
      logger.log(Level.FINER, JulMessageFormatter.format(message, args));
    }
  }

  @Override
  public void debug(String message) {
    logger.log(Level.FINE, message);
  }

  @Override
  public void debug(String message, Object... args) {
    if (logger.isLoggable(Level.FINE)) {
      logger.log(Level.FINE, JulMessageFormatter.format(message, args));
    }
  }

  @Override
  public void info(String message) {
    logger.log(Level.INFO, message);
  }

  @Override
  public void info(String message, Object... args) {
    if (logger.isLoggable(Level.INFO)) {
      logger.log(Level.INFO, JulMessageFormatter.format(message, args));
    }
  }

  @Override
  public void warn(String message) {
    logger.log(Level.WARNING, message);
  }

  @Override
  public void warn(String message, Object... args) {
    if (logger.isLoggable(Level.WARNING)) {
      logger.log(Level.WARNING, JulMessageFormatter.format(message, args));
    }
  }

  @Override
  public void error(String message) {
    logger.log(Level.SEVERE, message);
  }

  @Override
  public void error(String message, Object... args) {
    if (!logger.isLoggable(Level.SEVERE)) {
      return;
    }
    // 마지막 인자가 Throwable이면 스택트레이스를 살린다 (SLF4J 관례와 동일)
    logger.log(Level.SEVERE, JulMessageFormatter.format(message, args), lastThrowable(args));
  }

  @Override
  public void error(String message, Throwable throwable) {
    logger.log(Level.SEVERE, message, throwable);
  }

  private static Throwable lastThrowable(Object[] args) {
    if (args != null && args.length > 0 && args[args.length - 1] instanceof Throwable t) {
      return t;
    }
    return null;
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isLoggable(Level.FINER);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isLoggable(Level.FINE);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isLoggable(Level.INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isLoggable(Level.WARNING);
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isLoggable(Level.SEVERE);
  }
}

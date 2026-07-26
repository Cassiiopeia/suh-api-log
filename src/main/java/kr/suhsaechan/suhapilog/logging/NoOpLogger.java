package kr.suhsaechan.suhapilog.logging;

/**
 * suhapilog.logging.enabled=false 또는 backend=none 일 때 쓰는 무동작 로거.
 */
final class NoOpLogger implements SuhLogger {

  static final NoOpLogger INSTANCE = new NoOpLogger();

  private NoOpLogger() {
  }

  @Override public void trace(String message) { }

  @Override public void trace(String message, Object... args) { }

  @Override public void debug(String message) { }

  @Override public void debug(String message, Object... args) { }

  @Override public void info(String message) { }

  @Override public void info(String message, Object... args) { }

  @Override public void warn(String message) { }

  @Override public void warn(String message, Object... args) { }

  @Override public void error(String message) { }

  @Override public void error(String message, Object... args) { }

  @Override public void error(String message, Throwable throwable) { }

  @Override public boolean isTraceEnabled() { return false; }

  @Override public boolean isDebugEnabled() { return false; }

  @Override public boolean isInfoEnabled() { return false; }

  @Override public boolean isWarnEnabled() { return false; }

  @Override public boolean isErrorEnabled() { return false; }
}

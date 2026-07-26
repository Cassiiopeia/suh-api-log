package kr.suhsaechan.suhapilog.logging;

/**
 * enabled / minLevel 필터를 적용하고, 설정 변경 시 델리게이트를 다시 해석하는 데코레이터.
 *
 * <p>minLevel은 "추가 필터"다. 상위가 이미 막은 레벨을 이 옵션으로 열 수는 없다 —
 * 그건 상위 로깅 설정의 권한이며 라이브러리가 침범하지 않는다.
 */
final class GuardedLogger implements SuhLogger {

  private final String name;
  private final ClassLoader classLoader;

  private volatile SuhLogger delegate;
  private volatile int delegateGeneration = -1;

  GuardedLogger(String name, ClassLoader classLoader) {
    this.name = name;
    this.classLoader = classLoader;
  }

  private SuhLogger delegate() {
    int current = SuhLogSettings.generation();
    SuhLogger local = delegate;
    if (local == null || delegateGeneration != current) {
      local = LoggerBackendResolver.create(SuhLogSettings.getBackend(), name, classLoader);
      delegate = local;
      delegateGeneration = current;
    }
    return local;
  }

  /** 라이브러리 단 필터. 백엔드 레벨 판단은 델리게이트가 별도로 수행한다. */
  private boolean passes(LogLevel level) {
    if (!SuhLogSettings.isEnabled()) {
      return false;
    }
    LogLevel min = SuhLogSettings.getMinLevel();
    return min == null || level.isAtLeast(min);
  }

  @Override
  public void trace(String message) {
    if (passes(LogLevel.TRACE)) {
      delegate().trace(message);
    }
  }

  @Override
  public void trace(String message, Object... args) {
    if (passes(LogLevel.TRACE)) {
      delegate().trace(message, args);
    }
  }

  @Override
  public void debug(String message) {
    if (passes(LogLevel.DEBUG)) {
      delegate().debug(message);
    }
  }

  @Override
  public void debug(String message, Object... args) {
    if (passes(LogLevel.DEBUG)) {
      delegate().debug(message, args);
    }
  }

  @Override
  public void info(String message) {
    if (passes(LogLevel.INFO)) {
      delegate().info(message);
    }
  }

  @Override
  public void info(String message, Object... args) {
    if (passes(LogLevel.INFO)) {
      delegate().info(message, args);
    }
  }

  @Override
  public void warn(String message) {
    if (passes(LogLevel.WARN)) {
      delegate().warn(message);
    }
  }

  @Override
  public void warn(String message, Object... args) {
    if (passes(LogLevel.WARN)) {
      delegate().warn(message, args);
    }
  }

  @Override
  public void error(String message) {
    if (passes(LogLevel.ERROR)) {
      delegate().error(message);
    }
  }

  @Override
  public void error(String message, Object... args) {
    if (passes(LogLevel.ERROR)) {
      delegate().error(message, args);
    }
  }

  @Override
  public void error(String message, Throwable throwable) {
    if (passes(LogLevel.ERROR)) {
      delegate().error(message, throwable);
    }
  }

  @Override
  public boolean isTraceEnabled() {
    return passes(LogLevel.TRACE) && delegate().isTraceEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return passes(LogLevel.DEBUG) && delegate().isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return passes(LogLevel.INFO) && delegate().isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return passes(LogLevel.WARN) && delegate().isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return passes(LogLevel.ERROR) && delegate().isErrorEnabled();
  }
}

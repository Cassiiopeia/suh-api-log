package kr.suhsaechan.suhapilog.util;

import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;

/**
 * 하위 호환용 shim.
 *
 * @deprecated 3.0.0부터 {@link SuhLog}로 대체되었다. 이 클래스는 4.0.0에서 제거된다.
 *     기존 JUL 직접 사용과 달리, 이제 상위 프로젝트의 로깅 백엔드에 위임한다.
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class SuhApiLogger implements SuhLogger {

  private final SuhLogger delegate;

  private SuhApiLogger(SuhLogger delegate) {
    this.delegate = delegate;
  }

  public static SuhApiLogger getLogger(Class<?> clazz) {
    return new SuhApiLogger(SuhLog.getLogger(clazz));
  }

  public static SuhApiLogger getLogger(String name) {
    return new SuhApiLogger(SuhLog.getLogger(name));
  }

  @Override public void trace(String message) { delegate.trace(message); }

  @Override public void trace(String message, Object... args) { delegate.trace(message, args); }

  @Override public void debug(String message) { delegate.debug(message); }

  @Override public void debug(String message, Object... args) { delegate.debug(message, args); }

  @Override public void info(String message) { delegate.info(message); }

  @Override public void info(String message, Object... args) { delegate.info(message, args); }

  @Override public void warn(String message) { delegate.warn(message); }

  @Override public void warn(String message, Object... args) { delegate.warn(message, args); }

  @Override public void error(String message) { delegate.error(message); }

  @Override public void error(String message, Object... args) { delegate.error(message, args); }

  @Override
  public void error(String message, Throwable throwable) {
    delegate.error(message, throwable);
  }

  @Override public boolean isTraceEnabled() { return delegate.isTraceEnabled(); }

  @Override public boolean isDebugEnabled() { return delegate.isDebugEnabled(); }

  @Override public boolean isInfoEnabled() { return delegate.isInfoEnabled(); }

  @Override public boolean isWarnEnabled() { return delegate.isWarnEnabled(); }

  @Override public boolean isErrorEnabled() { return delegate.isErrorEnabled(); }
}

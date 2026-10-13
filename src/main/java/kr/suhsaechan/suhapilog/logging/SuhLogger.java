package kr.suhsaechan.suhapilog.logging;

/**
 * 라이브러리 내부 로깅 인터페이스.
 * 구현체는 상위 프로젝트에 존재하는 로깅 백엔드(SLF4J/Log4j2/JUL)에 위임한다.
 */
public interface SuhLogger {

  void trace(String message);

  void trace(String message, Object... args);

  void debug(String message);

  void debug(String message, Object... args);

  void info(String message);

  void info(String message, Object... args);

  void warn(String message);

  void warn(String message, Object... args);

  void error(String message);

  void error(String message, Object... args);

  void error(String message, Throwable throwable);

  boolean isTraceEnabled();

  boolean isDebugEnabled();

  boolean isInfoEnabled();

  boolean isWarnEnabled();

  boolean isErrorEnabled();
}

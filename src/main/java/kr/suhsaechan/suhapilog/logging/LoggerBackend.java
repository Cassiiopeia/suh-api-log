package kr.suhsaechan.suhapilog.logging;

/**
 * 로깅 백엔드 종류. {@code suhapilog.logging.backend} 설정값과 1:1 대응한다.
 */
public enum LoggerBackend {
  /** 클래스패스를 보고 자동 탐지 (기본값) */
  AUTO,
  SLF4J,
  LOG4J2,
  JUL,
  /** 라이브러리 로그 전면 차단 */
  NONE;

  public static LoggerBackend parseOrAuto(String value) {
    if (value == null || value.isBlank()) {
      return AUTO;
    }
    try {
      return LoggerBackend.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return AUTO;
    }
  }
}

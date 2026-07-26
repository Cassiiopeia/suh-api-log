package kr.suhsaechan.suhapilog.logging;

/**
 * 백엔드 중립 로그 레벨. 서열 비교로 {@code suhapilog.logging.level} 필터를 구현한다.
 */
public enum LogLevel {
  TRACE, DEBUG, INFO, WARN, ERROR;

  /** this가 other 이상인지 (ERROR.isAtLeast(WARN) == true) */
  public boolean isAtLeast(LogLevel other) {
    return this.ordinal() >= other.ordinal();
  }

  /** 설정값 파싱. 알 수 없는 값이면 null을 반환해 호출부가 "미설정"으로 처리하게 한다. */
  public static LogLevel parseOrNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LogLevel.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}

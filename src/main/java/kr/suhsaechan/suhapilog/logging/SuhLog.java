package kr.suhsaechan.suhapilog.logging;

/**
 * 라이브러리 로거 진입점.
 *
 * <pre>{@code
 * private static final SuhLogger log = SuhLog.getLogger(MyClass.class);
 * }</pre>
 *
 * <p>반환된 로거는 상위 프로젝트의 로깅 백엔드(log4j2 / logback / JUL)에 위임한다.
 * 아무 설정도 하지 않으면 상위 설정을 그대로 따른다.
 */
public final class SuhLog {

  private SuhLog() {
  }

  public static SuhLogger getLogger(Class<?> type) {
    return getLogger(type.getName());
  }

  public static SuhLogger getLogger(String name) {
    return new GuardedLogger(name, SuhLog.class.getClassLoader());
  }
}

package kr.suhsaechan.suhapilog.logging;

/**
 * 클래스패스를 보고 로깅 백엔드를 결정한다.
 *
 * <p>ClassLoader를 인자로 받는 이유는 테스트에서 FilteredClassLoader로 클래스패스
 * 변형을 재현하기 위해서다.
 */
final class LoggerBackendResolver {

  private LoggerBackendResolver() {
  }

  /**
   * 탐지 순서: SLF4J(바인딩 있음) → Log4j2 → JUL.
   *
   * <p>상위가 spring-boot-starter-log4j2를 쓰면 slf4j-api와 log4j-api가 모두 존재하고
   * SLF4J가 log4j2로 라우팅되므로, SLF4J를 1순위로 두어도 log4j2 설정이 적용된다.
   */
  static LoggerBackend detect(ClassLoader classLoader) {
    if (hasBoundSlf4j(classLoader)) {
      return LoggerBackend.SLF4J;
    }
    if (isPresent("org.apache.logging.log4j.LogManager", classLoader)) {
      return LoggerBackend.LOG4J2;
    }
    return LoggerBackend.JUL;
  }

  /**
   * SLF4J 2.x는 provider를 못 찾으면 NOPLoggerFactory를 돌려주고 로그를 전부 버린다.
   * 이 상태로 SLF4J를 고르면 로그가 통째로 사라지므로 다음 후보로 넘긴다.
   */
  private static boolean hasBoundSlf4j(ClassLoader classLoader) {
    try {
      Class<?> loggerFactory = Class.forName("org.slf4j.LoggerFactory", true, classLoader);
      Object factory = loggerFactory.getMethod("getILoggerFactory").invoke(null);
      return factory != null && !factory.getClass().getName().contains("NOP");
    } catch (Throwable e) {
      // ClassNotFound / LinkageError / provider 초기화 실패 전부 "SLF4J 사용 불가"로 본다
      return false;
    }
  }

  private static boolean isPresent(String className, ClassLoader classLoader) {
    try {
      Class.forName(className, false, classLoader);
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * 어댑터 클래스는 탐지 성공 이후에만 로드된다. 덕분에 slf4j-api가 클래스패스에
   * 없어도 Slf4jLoggerAdapter가 링크되지 않아 NoClassDefFoundError가 나지 않는다.
   */
  static SuhLogger create(LoggerBackend backend, String name, ClassLoader classLoader) {
    LoggerBackend resolved = (backend == null || backend == LoggerBackend.AUTO)
        ? detect(classLoader)
        : backend;

    try {
      return switch (resolved) {
        case NONE -> NoOpLogger.INSTANCE;
        case SLF4J -> new Slf4jLoggerAdapter(name);
        case LOG4J2 -> new Log4j2LoggerAdapter(name);
        default -> new JulLoggerAdapter(name);
      };
    } catch (Throwable e) {
      // 강제 지정한 백엔드가 실제로는 클래스패스에 없는 경우 — 로그를 잃지 않도록 JUL로 내린다
      return new JulLoggerAdapter(name);
    }
  }
}

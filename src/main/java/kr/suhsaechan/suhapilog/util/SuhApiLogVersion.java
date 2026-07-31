package kr.suhsaechan.suhapilog.util;

/**
 * 라이브러리 버전. User-Agent 문자열에 쓴다.
 *
 * <p>jar 매니페스트의 Implementation-Version을 읽고, IDE 실행이나 테스트처럼
 * 매니페스트가 없는 환경에서는 "dev"로 떨어진다.
 */
public final class SuhApiLogVersion {

  private static final String VERSION = resolve();

  private SuhApiLogVersion() {
  }

  public static String get() {
    return VERSION;
  }

  private static String resolve() {
    Package pkg = SuhApiLogVersion.class.getPackage();
    String version = (pkg == null) ? null : pkg.getImplementationVersion();
    return (version == null || version.isBlank()) ? "dev" : version;
  }
}

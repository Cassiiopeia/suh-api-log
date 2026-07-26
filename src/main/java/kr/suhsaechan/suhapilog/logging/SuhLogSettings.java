package kr.suhsaechan.suhapilog.logging;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 로깅 오버라이드 설정 홀더.
 *
 * <p>정적 로거는 Spring 컨텍스트보다 먼저 만들어지므로, 설정을 로거 인스턴스에
 * 박아두지 않고 이 홀더를 매 호출 시 읽는다. 설정이 바뀌면 generation이 올라가고
 * 각 로거가 델리게이트를 다시 해석한다.
 */
public final class SuhLogSettings {

  private static volatile boolean enabled = true;
  private static volatile LogLevel minLevel = null;
  private static volatile LoggerBackend backend = LoggerBackend.AUTO;
  private static final AtomicInteger GENERATION = new AtomicInteger();

  private SuhLogSettings() {
  }

  public static boolean isEnabled() {
    return enabled;
  }

  /** null이면 "제한 없음" — 백엔드 판단에 전적으로 맡긴다. */
  public static LogLevel getMinLevel() {
    return minLevel;
  }

  public static LoggerBackend getBackend() {
    return backend;
  }

  public static int generation() {
    return GENERATION.get();
  }

  public static void apply(boolean newEnabled, LogLevel newMinLevel, LoggerBackend newBackend) {
    enabled = newEnabled;
    minLevel = newMinLevel;
    backend = (newBackend == null) ? LoggerBackend.AUTO : newBackend;
    GENERATION.incrementAndGet();
  }

  /** 테스트 및 컨텍스트 재기동용 — 기본값(전부 상위 위임)으로 되돌린다. */
  public static void reset() {
    apply(true, null, LoggerBackend.AUTO);
  }
}

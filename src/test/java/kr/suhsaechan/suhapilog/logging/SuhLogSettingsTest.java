package kr.suhsaechan.suhapilog.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SuhLogSettingsTest {

  @AfterEach
  void tearDown() {
    SuhLogSettings.reset();
  }

  @Test
  void 기본값은_활성화되고_레벨_제한이_없으며_AUTO다() {
    assertThat(SuhLogSettings.isEnabled()).isTrue();
    assertThat(SuhLogSettings.getMinLevel()).isNull();
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.AUTO);
  }

  @Test
  void enabled가_false면_로거가_모든_레벨을_비활성으로_보고한다() {
    SuhLogger log = SuhLog.getLogger("t.disabled");
    assertThat(log.isErrorEnabled()).isTrue();

    SuhLogSettings.apply(false, null, LoggerBackend.AUTO);

    assertThat(log.isErrorEnabled()).isFalse();
    assertThat(log.isInfoEnabled()).isFalse();
    assertThat(log.isDebugEnabled()).isFalse();
    assertThatCode(() -> log.error("무시됨 {}", 1)).doesNotThrowAnyException();
  }

  @Test
  void minLevel_미만은_드롭하고_이상은_통과시킨다() {
    SuhLogger log = SuhLog.getLogger("t.level");
    SuhLogSettings.apply(true, LogLevel.WARN, LoggerBackend.AUTO);

    assertThat(log.isDebugEnabled()).isFalse();
    assertThat(log.isInfoEnabled()).isFalse();
    assertThat(log.isWarnEnabled()).isTrue();
    assertThat(log.isErrorEnabled()).isTrue();
  }

  @Test
  void 설정이_바뀌면_generation이_올라가고_로거가_델리게이트를_다시_해석한다() {
    int before = SuhLogSettings.generation();
    SuhLogger log = SuhLog.getLogger("t.gen");
    log.info("델리게이트 캐시 유발");

    SuhLogSettings.apply(true, null, LoggerBackend.JUL);

    assertThat(SuhLogSettings.generation()).isGreaterThan(before);
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.JUL);
    assertThatCode(() -> log.info("재해석 후에도 예외 없이 동작")).doesNotThrowAnyException();
  }

  @Test
  void backend를_NONE으로_강제하면_전부_비활성이_된다() {
    SuhLogger log = SuhLog.getLogger("t.none");
    SuhLogSettings.apply(true, null, LoggerBackend.NONE);

    assertThat(log.isErrorEnabled()).isFalse();
  }

  @Test
  void reset은_기본값으로_되돌린다() {
    SuhLogSettings.apply(false, LogLevel.ERROR, LoggerBackend.JUL);

    SuhLogSettings.reset();

    assertThat(SuhLogSettings.isEnabled()).isTrue();
    assertThat(SuhLogSettings.getMinLevel()).isNull();
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.AUTO);
  }

  @Test
  void 레벨_서열_비교가_올바르다() {
    assertThat(LogLevel.ERROR.isAtLeast(LogLevel.WARN)).isTrue();
    assertThat(LogLevel.DEBUG.isAtLeast(LogLevel.INFO)).isFalse();
    assertThat(LogLevel.INFO.isAtLeast(LogLevel.INFO)).isTrue();
    assertThat(LogLevel.parseOrNull("warn")).isEqualTo(LogLevel.WARN);
    assertThat(LogLevel.parseOrNull("없는값")).isNull();
    assertThat(LogLevel.parseOrNull(null)).isNull();
  }
}

package kr.suhsaechan.suhapilog.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import kr.suhsaechan.suhapilog.logging.LoggerBackend;
import kr.suhsaechan.suhapilog.logging.SuhLogSettings;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
class SuhApiLoggerShimTest {

  @AfterEach
  void tearDown() {
    SuhLogSettings.reset();
  }

  @Test
  void 기존_API가_그대로_동작한다() {
    SuhApiLogger log = SuhApiLogger.getLogger(SuhApiLoggerShimTest.class);

    assertThatCode(() -> {
      log.debug("디버그 {}", 1);
      log.info("인포");
      log.warn("경고 {} {}", 1, 2);
      log.error("에러", new IllegalStateException("테스트"));
    }).doesNotThrowAnyException();
  }

  @Test
  void SuhLogger_인터페이스를_구현해_새_코드와_섞어_쓸_수_있다() {
    assertThat(SuhApiLogger.getLogger("t")).isInstanceOf(SuhLogger.class);
  }

  @Test
  void 새_설정_홀더의_영향을_그대로_받는다() {
    SuhApiLogger log = SuhApiLogger.getLogger("t.shim");
    assertThat(log.isErrorEnabled()).isTrue();

    SuhLogSettings.apply(true, null, LoggerBackend.NONE);

    assertThat(log.isErrorEnabled()).isFalse();
  }
}

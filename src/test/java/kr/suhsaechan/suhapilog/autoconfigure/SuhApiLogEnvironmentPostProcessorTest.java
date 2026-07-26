package kr.suhsaechan.suhapilog.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import kr.suhsaechan.suhapilog.logging.LogLevel;
import kr.suhsaechan.suhapilog.logging.LoggerBackend;
import kr.suhsaechan.suhapilog.logging.SuhLogSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.mock.env.MockEnvironment;

class SuhApiLogEnvironmentPostProcessorTest {

  private final SuhApiLogEnvironmentPostProcessor processor =
      new SuhApiLogEnvironmentPostProcessor();

  @AfterEach
  void tearDown() {
    SuhLogSettings.reset();
  }

  @Test
  void 설정이_없으면_기본값을_유지한다() {
    processor.postProcessEnvironment(new MockEnvironment(), new SpringApplication());

    assertThat(SuhLogSettings.isEnabled()).isTrue();
    assertThat(SuhLogSettings.getMinLevel()).isNull();
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.AUTO);
  }

  @Test
  void 세_옵션을_모두_읽어_홀더에_반영한다() {
    MockEnvironment env = new MockEnvironment()
        .withProperty("suhapilog.logging.enabled", "false")
        .withProperty("suhapilog.logging.level", "warn")
        .withProperty("suhapilog.logging.backend", "jul");

    processor.postProcessEnvironment(env, new SpringApplication());

    assertThat(SuhLogSettings.isEnabled()).isFalse();
    assertThat(SuhLogSettings.getMinLevel()).isEqualTo(LogLevel.WARN);
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.JUL);
  }

  @Test
  void 알_수_없는_값은_기본값으로_떨어진다() {
    MockEnvironment env = new MockEnvironment()
        .withProperty("suhapilog.logging.level", "없는레벨")
        .withProperty("suhapilog.logging.backend", "없는백엔드");

    processor.postProcessEnvironment(env, new SpringApplication());

    assertThat(SuhLogSettings.getMinLevel()).isNull();
    assertThat(SuhLogSettings.getBackend()).isEqualTo(LoggerBackend.AUTO);
  }

  @Test
  void ConfigData보다_늦게_실행되도록_가장_낮은_우선순위를_가진다() {
    assertThat(processor.getOrder()).isEqualTo(Ordered.LOWEST_PRECEDENCE);
  }
}

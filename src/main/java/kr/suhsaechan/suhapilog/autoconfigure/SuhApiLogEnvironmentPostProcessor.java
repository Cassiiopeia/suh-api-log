package kr.suhsaechan.suhapilog.autoconfigure;

import kr.suhsaechan.suhapilog.logging.LogLevel;
import kr.suhsaechan.suhapilog.logging.LoggerBackend;
import kr.suhsaechan.suhapilog.logging.SuhLogSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 로깅 오버라이드 설정을 자동설정보다 훨씬 이른 시점에 주입한다.
 *
 * <p>라이브러리 로거는 static final로 클래스 로딩 시 만들어지므로, 빈 생성 이후에
 * 설정을 읽으면 이미 늦다. LOWEST_PRECEDENCE로 등록해 ConfigDataEnvironmentPostProcessor가
 * application.yml을 로드한 뒤에 실행되도록 한다.
 */
public class SuhApiLogEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final String PREFIX = "suhapilog.logging.";

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    boolean enabled = environment.getProperty(PREFIX + "enabled", Boolean.class, Boolean.TRUE);
    LogLevel minLevel = LogLevel.parseOrNull(environment.getProperty(PREFIX + "level"));
    LoggerBackend backend = LoggerBackend.parseOrAuto(environment.getProperty(PREFIX + "backend"));

    SuhLogSettings.apply(enabled, minLevel, backend);
  }

  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}

package kr.suhsaechan.suhapilog.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import kr.suhsaechan.suhapilog.autoconfigure.ApiChangeLogProperties.StartupMode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ApiChangeLogPropertiesTest {

  private final ApplicationContextRunner runner = new ApplicationContextRunner()
      .withUserConfiguration(Config.class);

  @EnableConfigurationProperties(ApiChangeLogProperties.class)
  static class Config {
  }

  @Test
  void 기본값이_스펙과_일치한다() {
    runner.run(context -> {
      ApiChangeLogProperties props = context.getBean(ApiChangeLogProperties.class);

      assertThat(props.isEnabled()).isTrue();
      assertThat(props.getDefaultAuthor()).isEmpty();
      assertThat(props.getStorage().getPath()).isEqualTo("./github-issues");

      assertThat(props.getGithub().getBaseUrl()).isNull();
      assertThat(props.getGithub().getTimeout()).isEqualTo(Duration.ofSeconds(5));
      assertThat(props.getGithub().getConcurrency()).isEqualTo(4);
      assertThat(props.getGithub().getStartupMode()).isEqualTo(StartupMode.ASYNC);
      assertThat(props.getGithub().isFailFast()).isFalse();
      assertThat(props.getGithub().getRetry().getMaxAttempts()).isEqualTo(2);
      assertThat(props.getGithub().getRetry().getBackoff()).isEqualTo(Duration.ofMillis(500));
      assertThat(props.getGithub().getCache().getTtl()).isNull();
      assertThat(props.getGithub().getCache().getNegativeTtl()).isEqualTo(Duration.ofMinutes(10));

      assertThat(props.getRender().getColumns())
          .isEqualTo("date,author,issue-number,issue-title,description");
      assertThat(props.getRender().getSort()).isEqualTo("date-desc");
      assertThat(props.getRender().getTitle()).isEqualTo("API 변경 이력");
      assertThat(props.getRender().isEscapeHtml()).isTrue();

      assertThat(props.getLogging().isEnabled()).isTrue();
      assertThat(props.getLogging().getLevel()).isNull();
      assertThat(props.getLogging().getBackend()).isEqualTo("auto");
    });
  }

  @Test
  void 모든_키를_yml_스타일로_바인딩한다() {
    runner.withPropertyValues(
        "suhapilog.enabled=false",
        "suhapilog.default-author=서새찬",
        "suhapilog.storage.path=/tmp/issues",
        "suhapilog.github.base-url=https://github.com/o/r/issues/",
        "suhapilog.github.timeout=2s",
        "suhapilog.github.concurrency=8",
        "suhapilog.github.startup-mode=lazy",
        "suhapilog.github.fail-fast=true",
        "suhapilog.github.user-agent=my-agent",
        "suhapilog.github.retry.max-attempts=5",
        "suhapilog.github.retry.backoff=1s",
        "suhapilog.github.cache.ttl=1h",
        "suhapilog.github.cache.negative-ttl=30s",
        "suhapilog.render.columns=date,description",
        "suhapilog.render.sort=date-asc",
        "suhapilog.render.title=변경 이력",
        "suhapilog.render.escape-html=false",
        "suhapilog.logging.enabled=false",
        "suhapilog.logging.level=warn",
        "suhapilog.logging.backend=jul"
    ).run(context -> {
      ApiChangeLogProperties props = context.getBean(ApiChangeLogProperties.class);

      assertThat(props.isEnabled()).isFalse();
      assertThat(props.getDefaultAuthor()).isEqualTo("서새찬");
      assertThat(props.getStorage().getPath()).isEqualTo("/tmp/issues");
      assertThat(props.getGithub().getBaseUrl()).isEqualTo("https://github.com/o/r/issues/");
      assertThat(props.getGithub().getTimeout()).isEqualTo(Duration.ofSeconds(2));
      assertThat(props.getGithub().getConcurrency()).isEqualTo(8);
      assertThat(props.getGithub().getStartupMode()).isEqualTo(StartupMode.LAZY);
      assertThat(props.getGithub().isFailFast()).isTrue();
      assertThat(props.getGithub().getUserAgent()).isEqualTo("my-agent");
      assertThat(props.getGithub().getRetry().getMaxAttempts()).isEqualTo(5);
      assertThat(props.getGithub().getRetry().getBackoff()).isEqualTo(Duration.ofSeconds(1));
      assertThat(props.getGithub().getCache().getTtl()).isEqualTo(Duration.ofHours(1));
      assertThat(props.getGithub().getCache().getNegativeTtl()).isEqualTo(Duration.ofSeconds(30));
      assertThat(props.getRender().getColumns()).isEqualTo("date,description");
      assertThat(props.getRender().getSort()).isEqualTo("date-asc");
      assertThat(props.getRender().getTitle()).isEqualTo("변경 이력");
      assertThat(props.getRender().isEscapeHtml()).isFalse();
      assertThat(props.getLogging().isEnabled()).isFalse();
      assertThat(props.getLogging().getLevel()).isEqualTo("warn");
      assertThat(props.getLogging().getBackend()).isEqualTo("jul");
    });
  }
}

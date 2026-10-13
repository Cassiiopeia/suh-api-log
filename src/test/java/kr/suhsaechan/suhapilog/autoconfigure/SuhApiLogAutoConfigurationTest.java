package kr.suhsaechan.suhapilog.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import kr.suhsaechan.suhapilog.core.ChangelogService;
import kr.suhsaechan.suhapilog.testsupport.InMemoryIssueRepository;
import kr.suhsaechan.suhapilog.fetch.JsoupIssueFetcher;
import kr.suhsaechan.suhapilog.integration.SpringDocOperationCustomizer;
import kr.suhsaechan.suhapilog.render.DefaultTitleNormalizer;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer;
import kr.suhsaechan.suhapilog.spi.ApiLogEntry;
import kr.suhsaechan.suhapilog.spi.ChangelogRenderer;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.IssueRepository;
import kr.suhsaechan.suhapilog.spi.TitleNormalizer;
import kr.suhsaechan.suhapilog.storage.JsonIssueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class SuhApiLogAutoConfigurationTest {

  @TempDir
  static Path tempDir;

  private ApplicationContextRunner runner() {
    return new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SuhApiLogAutoConfiguration.class))
        .withPropertyValues(
            "suhapilog.storage.path=" + tempDir.toAbsolutePath(),
            "suhapilog.github.base-url=https://github.com/o/r/issues/",
            "suhapilog.github.startup-mode=lazy");
  }

  @Test
  void 기본_구현_빈이_전부_등록된다() {
    runner().run(context -> {
      assertThat(context).hasSingleBean(ApiChangeLogProperties.class);
      assertThat(context).hasSingleBean(ChangelogService.class);
      assertThat(context.getBean(TitleNormalizer.class))
          .isInstanceOf(DefaultTitleNormalizer.class);
      assertThat(context.getBean(IssueRepository.class))
          .isInstanceOf(JsonIssueRepository.class);
      assertThat(context.getBean(IssueFetcher.class)).isInstanceOf(JsoupIssueFetcher.class);
      assertThat(context.getBean(ChangelogRenderer.class))
          .isInstanceOf(HtmlTableChangelogRenderer.class);
    });
  }

  @Test
  void enabled_false면_아무_빈도_만들지_않는다() {
    runner().withPropertyValues("suhapilog.enabled=false").run(context ->
        assertThat(context).doesNotHaveBean(ChangelogService.class));
  }

  @Configuration(proxyBeanMethods = false)
  static class CustomSpi {

    @Bean
    TitleNormalizer myNormalizer() {
      return raw -> "CUSTOM";
    }

    @Bean
    IssueFetcher myFetcher() {
      return n -> Optional.<IssueRef>empty();
    }

    @Bean
    ChangelogRenderer myRenderer() {
      return entries -> "CUSTOM_RENDER";
    }

    @Bean
    IssueRepository myRepository() {
      return new InMemoryIssueRepository();
    }
  }

  @Test
  void SPI_4종은_사용자_빈이_있으면_기본_구현이_물러난다() {
    runner().withUserConfiguration(CustomSpi.class).run(context -> {
      assertThat(context.getBean(TitleNormalizer.class).normalize("무엇이든"))
          .isEqualTo("CUSTOM");
      assertThat(context.getBean(ChangelogRenderer.class).render(List.<ApiLogEntry>of()))
          .isEqualTo("CUSTOM_RENDER");
      assertThat(context.getBean(IssueFetcher.class)).isNotInstanceOf(JsoupIssueFetcher.class);
      assertThat(context.getBean(IssueRepository.class))
          .isNotInstanceOf(JsonIssueRepository.class);
    });
  }

  @Test
  void base_url이_없어도_기본값에서는_컨텍스트가_뜬다() {
    // 레거시는 IllegalStateException으로 상위 앱 부팅을 죽였다
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SuhApiLogAutoConfiguration.class))
        .withPropertyValues("suhapilog.storage.path=" + tempDir.toAbsolutePath())
        .run(context -> assertThat(context).hasNotFailed());
  }

  @Test
  void base_url이_없으면_러너가_경고만_남기고_조용히_비활성화된다() throws Exception {
    // ApplicationContextRunner는 ApplicationRunner를 실행하지 않으므로 직접 호출해 검증한다
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SuhApiLogAutoConfiguration.class))
        .withPropertyValues("suhapilog.storage.path=" + tempDir.toAbsolutePath())
        .run(context -> {
          ApplicationRunner runner = context.getBean(ApplicationRunner.class);
          assertThatCode(() -> runner.run(new DefaultApplicationArguments()))
              .doesNotThrowAnyException();
        });
  }

  @Test
  void fail_fast가_true면_base_url_누락_시_러너가_예외를_던진다() {
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SuhApiLogAutoConfiguration.class))
        .withPropertyValues(
            "suhapilog.storage.path=" + tempDir.toAbsolutePath(),
            "suhapilog.github.fail-fast=true")
        .run(context -> {
          ApplicationRunner runner = context.getBean(ApplicationRunner.class);
          assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
              .isInstanceOf(IllegalStateException.class)
              .hasMessageContaining("suhapilog.github.base-url");
        });
  }

  @Test
  void springdoc이_없으면_커스터마이저_빈을_만들지_않는다() {
    runner()
        .withClassLoader(new FilteredClassLoader("org.springdoc.core.customizers"))
        .run(context -> {
          assertThat(context).hasNotFailed();
          assertThat(context).doesNotHaveBean(SpringDocOperationCustomizer.class);
        });
  }

  @Test
  void springdoc이_있으면_커스터마이저_빈을_만든다() {
    runner().run(context ->
        assertThat(context).hasSingleBean(SpringDocOperationCustomizer.class));
  }
}

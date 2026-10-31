package kr.suhsaechan.suhapilog.autoconfigure;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import kr.suhsaechan.suhapilog.autoconfigure.ApiChangeLogProperties.StartupMode;
import kr.suhsaechan.suhapilog.core.ChangelogService;
import kr.suhsaechan.suhapilog.core.ControllerScanner;
import kr.suhsaechan.suhapilog.core.IssueResolver;
import kr.suhsaechan.suhapilog.core.IssueSyncCoordinator;
import kr.suhsaechan.suhapilog.fetch.JsoupIssueFetcher;
import kr.suhsaechan.suhapilog.integration.SpringDocOperationCustomizer;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import kr.suhsaechan.suhapilog.render.DefaultTitleNormalizer;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer;
import kr.suhsaechan.suhapilog.spi.ChangelogRenderer;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRepository;
import kr.suhsaechan.suhapilog.spi.TitleNormalizer;
import kr.suhsaechan.suhapilog.storage.JsonIssueRepository;
import kr.suhsaechan.suhapilog.util.ProjectRootResolver;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * suh-api-log 자동설정.
 *
 * <p>SPI 4종은 모두 {@code @ConditionalOnMissingBean}이라, 상위 프로젝트가 같은 타입
 * 빈 하나만 등록하면 기본 구현이 물러난다.
 */
@AutoConfiguration
@EnableConfigurationProperties(ApiChangeLogProperties.class)
@ConditionalOnProperty(prefix = "suhapilog", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class SuhApiLogAutoConfiguration {

  private static final SuhLogger log = SuhLog.getLogger(SuhApiLogAutoConfiguration.class);

  @Bean
  @ConditionalOnMissingBean
  public TitleNormalizer suhApiLogTitleNormalizer() {
    return new DefaultTitleNormalizer();
  }

  @Bean
  @ConditionalOnMissingBean
  public IssueRepository suhApiLogIssueRepository(ApiChangeLogProperties properties) {
    String path = ProjectRootResolver
        .resolveStoragePath(properties.getStorage().getPath())
        .toString();
    return new JsonIssueRepository(path);
  }

  @Bean
  @ConditionalOnMissingBean
  public IssueFetcher suhApiLogIssueFetcher(ApiChangeLogProperties properties,
      TitleNormalizer titleNormalizer) {
    ApiChangeLogProperties.Github github = properties.getGithub();
    String userAgent = (github.getUserAgent() == null || github.getUserAgent().isBlank())
        ? JsoupIssueFetcher.defaultUserAgent()
        : github.getUserAgent();

    JsoupIssueFetcher.Options options = new JsoupIssueFetcher.Options(
        github.getTimeout(),
        github.getRetry().getMaxAttempts(),
        github.getRetry().getBackoff(),
        userAgent,
        github.getCache().getNegativeTtl());

    return new JsoupIssueFetcher(github.getBaseUrl(), titleNormalizer, options);
  }

  @Bean
  @ConditionalOnMissingBean
  public ChangelogRenderer suhApiLogChangelogRenderer(ApiChangeLogProperties properties) {
    ApiChangeLogProperties.Render render = properties.getRender();
    HtmlTableChangelogRenderer.Options options = new HtmlTableChangelogRenderer.Options(
        HtmlTableChangelogRenderer.Column.parse(render.getColumns()),
        HtmlTableChangelogRenderer.Sort.parseOrDefault(render.getSort()),
        render.getTitle(),
        render.isEscapeHtml());
    return new HtmlTableChangelogRenderer(options);
  }

  @Bean
  @ConditionalOnMissingBean
  public IssueResolver suhApiLogIssueResolver(IssueRepository repository, IssueFetcher fetcher,
      ApiChangeLogProperties properties) {
    Duration ttl = properties.getGithub().getCache().getTtl();
    return new IssueResolver(repository, fetcher, ttl);
  }

  @Bean
  @ConditionalOnMissingBean
  public ChangelogService suhApiLogChangelogService(IssueResolver issueResolver,
      ChangelogRenderer renderer, ApiChangeLogProperties properties) {
    return new ChangelogService(issueResolver, renderer,
        properties.getGithub().getBaseUrl(), properties.getDefaultAuthor());
  }

  @Bean
  @ConditionalOnMissingBean
  public IssueSyncCoordinator suhApiLogIssueSyncCoordinator(IssueResolver issueResolver,
      IssueRepository repository, ApiChangeLogProperties properties) {
    return new IssueSyncCoordinator(issueResolver, repository,
        properties.getGithub().getConcurrency());
  }

  /**
   * 부팅 시 이슈 캐시 워밍업.
   *
   * <p>기본 startup-mode가 ASYNC라 부팅을 막지 않는다. base-url이 없으면 경고만 남기고
   * 이슈 연동을 끈다 — fail-fast=true일 때만 예외를 던진다.
   */
  @Bean
  public ApplicationRunner suhApiLogChangelogInitializer(IssueSyncCoordinator coordinator,
      ApplicationContext context, ApiChangeLogProperties properties) {
    return args -> {
      ApiChangeLogProperties.Github github = properties.getGithub();

      if (github.getBaseUrl() == null || github.getBaseUrl().isBlank()) {
        String message = "suhapilog.github.base-url이 설정되지 않아 이슈 연동을 비활성화합니다. "
            + "application.yml에 suhapilog.github.base-url="
            + "https://github.com/{owner}/{repo}/issues/ 를 추가하세요.";
        if (github.isFailFast()) {
          throw new IllegalStateException(message);
        }
        log.warn(message);
        return;
      }

      if (github.getStartupMode() == StartupMode.LAZY) {
        log.debug("startup-mode=lazy — 부팅 시 이슈 동기화를 건너뜁니다");
        return;
      }

      Class<?>[] controllers = collectControllerClasses(context);
      Runnable task = () -> {
        try {
          coordinator.sync(controllers);
        } catch (Exception e) {
          log.error("API 변경 이력 동기화 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
      };

      if (github.getStartupMode() == StartupMode.BLOCKING) {
        task.run();
      } else {
        Thread thread = new Thread(task, "suh-api-log-startup-sync");
        thread.setDaemon(true); // 동기화가 늦어져도 JVM 종료를 막지 않는다
        thread.start();
      }
    };
  }

  /**
   * 컨트롤러 클래스를 수집한다.
   *
   * <p>빈 인스턴스가 아니라 빈 <b>정의</b>의 타입을 읽는다 (컨트롤러 조기 초기화 방지)
   */
  private Class<?>[] collectControllerClasses(ApplicationContext context) {
    Set<Class<?>> classes = new LinkedHashSet<>();
    for (String name : context.getBeanNamesForAnnotation(RestController.class)) {
      addType(context, classes, name);
    }
    for (String name : context.getBeanNamesForAnnotation(Controller.class)) {
      addType(context, classes, name);
    }
    return classes.toArray(Class<?>[]::new);
  }

  private void addType(ApplicationContext context, Set<Class<?>> classes, String name) {
    Class<?> type = context.getType(name);
    if (type != null) {
      classes.add(ControllerScanner.unwrap(type));
    }
  }

  /**
   * springdoc이 클래스패스에 있을 때만 활성화된다.
   */
  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(OperationCustomizer.class)
  static class SpringDocIntegrationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SpringDocOperationCustomizer suhApiLogOperationCustomizer(ChangelogService changelogService) {
      return new SpringDocOperationCustomizer(changelogService);
    }
  }
}

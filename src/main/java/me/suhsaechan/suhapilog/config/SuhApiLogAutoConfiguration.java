package me.suhsaechan.suhapilog.config;

import java.util.Map;
import me.suhsaechan.suhapilog.service.ChangelogProcessor;
import me.suhsaechan.suhapilog.service.GithubIssueService;
import me.suhsaechan.suhapilog.storage.IssueRepository;
import me.suhsaechan.suhapilog.storage.JsonIssueRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * API Changelog 자동 설정 클래스
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "suhapilog", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfiguration
public class SuhApiLogAutoConfiguration {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(SuhApiLogAutoConfiguration.class);

  @Bean
  @ConfigurationProperties(prefix = "suhapilog")
  public ApiChangeLogProperties apiChangeLogProperties() {
    return new ApiChangeLogProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public IssueRepository issueRepository(ApiChangeLogProperties properties) {
    return new JsonIssueRepository(properties.getStorage().getPath());
  }

  @Bean
  @ConditionalOnMissingBean
  public GithubIssueService githubIssueService(
      IssueRepository issueRepository,
      ApiChangeLogProperties properties) {

    return new GithubIssueService(
        issueRepository,
        properties.getGithub().getBaseUrl(),
        null,
        properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public ChangelogProcessor changelogProcessor(GithubIssueService githubIssueService) {
    return new ChangelogProcessor(githubIssueService);
  }

  @Bean
  public Object operationCustomizer(final ChangelogProcessor processor) {
    log.debug("SpringDoc 통합 시도 중...");
    try {
      // SpringDoc 존재 여부 확인 - 최신 버전 경로 확인
      Class<?> customizer;
      try {
        log.debug("SpringDoc 3.x 경로 확인: org.springdoc.core.customizers.OperationCustomizer");
        customizer = Class.forName("org.springdoc.core.customizers.OperationCustomizer");
        log.debug("SpringDoc 3.x 버전 발견");
      } catch (ClassNotFoundException e) {
        // 최신 SpringDoc 버전 (2.x) 경로 시도
        try {
          log.debug("SpringDoc 2.x 경로 확인: org.springdoc.api.customizers.OperationCustomizer");
          customizer = Class.forName("org.springdoc.api.customizers.OperationCustomizer");
          log.debug("SpringDoc 2.x 버전 발견");
        } catch (ClassNotFoundException ex) {
          log.warn("SpringDoc OperationCustomizer 클래스를 찾을 수 없습니다");
          return null;
        }
      }

      // 로그 확인
      log.debug("클래스로더: {}", getClass().getClassLoader());
      log.debug("의존성 확인: {}", customizer.getName());

      // 런타임에 OperationCustomizer 인터페이스를 구현
      Object instance = java.lang.reflect.Proxy.newProxyInstance(
          getClass().getClassLoader(),
          new Class<?>[]{customizer},
          (proxy, method, args) -> {
            // customize 메서드인 경우 처리
            if ("customize".equals(method.getName()) && args.length == 2) {
              return processor.customizeSwaggerOperation(args[0], args[1]);
            }
            // Object 클래스의 메서드는 프록시 객체에 맞게 직접 구현
            if (method.getDeclaringClass() == Object.class) {
              if ("toString".equals(method.getName())) {
                return "OperationCustomizer Proxy";
              } else if ("equals".equals(method.getName())) {
                return proxy == args[0];
              } else if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
              }
            }
            return null;
          });

      log.info("Swagger OperationCustomizer 생성됨 - API 변경 이력이 Swagger UI에 표시됩니다");
      return instance;
    } catch (Exception e) {
      log.warn("Swagger 통합 구성에 실패했습니다: {}", e.getMessage(), e);
      return null;
    }
  }

  @Bean
  public ApplicationRunner apiChangelogInitializer(
      final ChangelogProcessor changelogProcessor,
      final ApplicationContext context,
      final ApiChangeLogProperties properties) {

    return args -> {

      // 설정 유효성 검증
      if (properties.isEnabled() && (properties.getGithub().getBaseUrl() == null || properties.getGithub().getBaseUrl()
          .isEmpty())) {
        throw new IllegalStateException(
            "GitHub 이슈 기능이 활성화되었지만 baseUrl이 설정되지 않았습니다.\n" +
                "application.yml 또는 application.properties에 다음을 추가하세요:\n" +
                "suhapilog.github.base-url=https://github.com/YourUsername/YourRepo/issues/");
      }

      log.info("API 변경 이력 정보 초기화 시작...");
      try {
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        controllers.putAll(context.getBeansWithAnnotation(Controller.class));

        // 원본 컨트롤러 클래스 수집 (CGLIB 프록시가 아닌)
        Class<?>[] sourceClasses = controllers.values().stream()
            .map(controller -> {
              Class<?> clazz = controller.getClass();
              // CGLIB 프록시라면 원본 클래스 사용
              if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
              }
              log.debug("원본 컨트롤러 클래스: {}", clazz.getName());
              return clazz;
            })
            .toArray(Class<?>[]::new);

        changelogProcessor.syncIssuesFromSource(sourceClasses);
        log.info("API 변경 이력 정보 초기화 완료");
      } catch (Exception e) {
        log.error("API 변경 이력 초기화 중 오류 발생: {}", e.getMessage(), e);
      }
    };
  }
}
package me.suhsaechan.suhapilog.config;

import java.util.Map;
import me.suhsaechan.suhapilog.service.ChangelogProcessor;
import me.suhsaechan.suhapilog.service.GithubIssueService;
import me.suhsaechan.suhapilog.storage.IssueRepository;
import me.suhsaechan.suhapilog.storage.JsonIssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
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
public class SuhApiLogAutoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(SuhApiLogAutoConfiguration.class);

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
            // 그 외 메서드는 기본 처리
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
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
      final ApplicationContext context) {

    return args -> {
      log.info("API 변경 이력 정보 초기화 시작...");
      try {
        // 모든 컨트롤러 클래스 찾기
        Map<String, Object> controllers = context.getBeansWithAnnotation(RestController.class);
        controllers.putAll(context.getBeansWithAnnotation(Controller.class));

        // 클래스 배열로 변환
        Class<?>[] controllerClasses = controllers.values().stream()
            .map(Object::getClass)
            .toArray(Class<?>[]::new);

        // 모든 컨트롤러에서 API 변경 이력 스캔 및 GitHub 이슈 정보 가져오기
        changelogProcessor.syncIssuesFromSource(controllerClasses);
        log.info("API 변경 이력 정보 초기화 완료");
      } catch (Exception e) {
        log.error("API 변경 이력 초기화 중 오류 발생: {}", e.getMessage(), e);
      }
    };
  }
}
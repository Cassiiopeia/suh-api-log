package me.suhsaechan.suhapilog.config;

import me.suhsaechan.suhapilog.service.ChangelogProcessor;
import me.suhsaechan.suhapilog.service.GithubIssueService;
import me.suhsaechan.suhapilog.storage.IssueRepository;
import me.suhsaechan.suhapilog.storage.JsonIssueRepository;

/**
 * API 변경 이력 모듈의 설정 클래스
 * 사용자는 이 클래스를 상속하여 자신의 @Configuration 클래스를 만들 수 있음
 */
class ApiChangeLogConfiguration {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(SuhApiLogAutoConfiguration.class);

  /**
   * 기본 속성 생성 (기본값 사용)
   */
  protected ApiChangeLogProperties properties() {
    return new ApiChangeLogProperties();
  }

  /**
   * 이슈 저장소 생성
   */
  public IssueRepository issueRepository(ApiChangeLogProperties properties) {
    return new JsonIssueRepository(properties.getStorage().getPath());
  }

  /**
   * GitHub 이슈 서비스 생성
   */
  public GithubIssueService githubIssueService(
      IssueRepository issueRepository,
      ApiChangeLogProperties properties) {

    return new GithubIssueService(
        issueRepository,
        properties.getGithub().getBaseUrl(),
        null,
        properties);
  }

  /**
   * 변경 로그 처리기 생성
   */
  public ChangelogProcessor changelogProcessor(GithubIssueService githubIssueService) {
    return new ChangelogProcessor(githubIssueService);
  }

  /**
   * SwaggerOperationCustomizer Bean 생성 시도 (SpringDoc이 존재할 때만 작동)
   * 이 메서드는 Spring에서 자동으로 호출되므로 별도의 호출은 필요 없음
   */
  public Object operationCustomizer(final ChangelogProcessor processor) {
    try {
      // SpringDoc 존재 여부 확인
      Class<?> customizer = Class.forName("org.springdoc.core.customizers.OperationCustomizer");

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
    } catch (ClassNotFoundException e) {
      // SpringDoc이 존재하지 않는 경우 무시
      log.debug("SpringDoc 의존성을 찾을 수 없습니다. Swagger UI에 변경 이력이 표시되지 않습니다");
      return null;
    }
  }
}
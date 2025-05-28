package me.suhsaechan.suhapilog.service;

import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.web.method.HandlerMethod;

/**
 * Swagger와의 통합을 위한 클래스
 * 이 클래스는 Swagger/SpringDoc이 존재할 때만 실제로 작동하도록 설계됨
 */
public class SwaggerIntegration {
  private static final Logger log = LoggerFactory.getLogger(SwaggerIntegration.class);

  private final GithubIssueService githubIssueService;

  public SwaggerIntegration(GithubIssueService githubIssueService) {
    this.githubIssueService = githubIssueService;
  }

  /**
   * Swagger Operation 객체를 커스터마이즈
   * 이 메서드는 리플렉션을 통해 간접적으로 호출됨 (Swagger/SpringDoc 의존성이 있을 때만)
   */
  public Object customizeOperation(Object operation, HandlerMethod handlerMethod) {
    try {
      // Swagger 의존성이 있는지 확인
      Class<?> operationClass = operation.getClass();
      java.lang.reflect.Method descriptionGetter = operationClass.getMethod("getDescription");
      java.lang.reflect.Method descriptionSetter = operationClass.getMethod("setDescription", String.class);

      // 기존 설명 가져오기
      String originalDescription = (String) descriptionGetter.invoke(operation);
      if (originalDescription == null) originalDescription = "";

      // 어노테이션 스캔
      MergedAnnotations annotations = MergedAnnotations.from(
          handlerMethod.getMethod(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);

      if (annotations.isPresent(ApiChangeLogs.class)) {
        // 변경 로그 HTML 생성
        String changelogHtml = githubIssueService.generateChangelogHtml(handlerMethod.getMethod());

        // 설명에 변경 로그 추가
        descriptionSetter.invoke(operation, originalDescription + changelogHtml);
      }
    } catch (Exception e) {
      log.debug("Swagger 통합 실패 (Swagger 의존성이 없거나 예외 발생): {}", e.getMessage());
    }

    return operation;
  }
}
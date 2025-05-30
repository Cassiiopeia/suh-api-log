package me.suhsaechan.suhapilog.service;

import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.web.method.HandlerMethod;

/**
 * 변경 로그 처리를 위한 핵심 로직
 */
public class ChangelogProcessor {
  private static final Logger log = LoggerFactory.getLogger(ChangelogProcessor.class);

  private final GithubIssueService githubIssueService;
  private final SwaggerIntegration swaggerIntegration;

  public ChangelogProcessor(GithubIssueService githubIssueService) {
    this.githubIssueService = githubIssueService;
    this.swaggerIntegration = new SwaggerIntegration(githubIssueService);
  }

  /**
   * 메서드에서 변경 로그 HTML 생성
   */
  public String generateChangelogHtml(Method method) {
    return githubIssueService.generateChangelogHtml(method);
  }

  /**
   * Swagger Operation 객체를 커스터마이즈
   */
  public Object customizeSwaggerOperation(Object operation, Object handlerMethod) {
    if (operation == null || handlerMethod == null) {
      return operation;
    }

    // HandlerMethod 타입으로 변환 가능한지 확인
    if (handlerMethod instanceof HandlerMethod) {
      return swaggerIntegration.customizeOperation(operation, (HandlerMethod) handlerMethod);
    }

    return operation;
  }

  /**
   * 주어진 소스에서 변경 로그를 스캔하여 이슈 번호 추출
   */
  public void syncIssuesFromSource(Class<?>[] sources) {
    Set<Integer> allIssueNumbers = new TreeSet<>();

    for (Class<?> source : sources) {
      for (Method method : source.getDeclaredMethods()) {
        if (method.isAnnotationPresent(ApiChangeLogs.class)) {
          ApiChangeLogs apiChangeLogs = method.getAnnotation(ApiChangeLogs.class);
          for (int i = 0; i < apiChangeLogs.value().length; i++) {
            int issueNumber = apiChangeLogs.value()[i].issueNumber();
            if (issueNumber > 0) {
              allIssueNumbers.add(issueNumber);
            }
          }
        }
      }
    }

    if (!allIssueNumbers.isEmpty()) {
      log.debug("클래스에서 발견된 이슈 번호: {}", allIssueNumbers);
      githubIssueService.syncIssues(allIssueNumbers);
    }
  }
}
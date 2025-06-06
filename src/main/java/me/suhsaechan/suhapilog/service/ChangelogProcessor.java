package me.suhsaechan.suhapilog.service;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import me.suhsaechan.suhapilog.config.SuhApiLogAutoConfiguration;
import me.suhsaechan.suhapilog.config.SuhApiLogger;
import org.springframework.web.method.HandlerMethod;

/**
 * 변경 로그 처리를 위한 핵심 로직
 */
public class ChangelogProcessor {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(SuhApiLogAutoConfiguration.class);

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
  log.debug("컨트롤러 클래스 스캔 시작 - 총 {}개 클래스", sources.length);

  for (Class<?> source : sources) {
    log.debug("클래스 검사: {}", source.getName());

    // 1. 인터페이스도 포함하여 모든 메서드 가져오기
    for (Method method : source.getMethods()) {
      ApiChangeLogs apiChangeLogs = null;

      // 직접 메서드에서 어노테이션 확인
      if (method.isAnnotationPresent(ApiChangeLogs.class)) {
        apiChangeLogs = method.getAnnotation(ApiChangeLogs.class);
        log.debug("메서드 {}에서 직접 @ApiChangeLogs 발견", method.getName());
      } else {
        // 인터페이스에서 동일한 메서드 찾기
        try {
          for (Class<?> iface : source.getInterfaces()) {
            try {
              Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
              if (ifaceMethod.isAnnotationPresent(ApiChangeLogs.class)) {
                apiChangeLogs = ifaceMethod.getAnnotation(ApiChangeLogs.class);
                log.debug("인터페이스 {} 메서드 {}에서 @ApiChangeLogs 발견",
                         iface.getSimpleName(), method.getName());
                break;
              }
            } catch (NoSuchMethodException e) {
              // 인터페이스에 해당 메서드가 없으면 무시
            }
          }
        } catch (Exception e) {
          log.debug("인터페이스 검사 중 예외 발생: {}", e.getMessage());
        }
      }

      // 발견된 어노테이션 처리
      if (apiChangeLogs != null) {
        for (ApiChangeLog changeLog : apiChangeLogs.value()) {
          int issueNumber = changeLog.issueNumber();
          if (issueNumber > 0) {
            allIssueNumbers.add(issueNumber);
            log.debug("이슈 번호 추가: {}", issueNumber);
          }
        }
      }
    }
  }

  if (!allIssueNumbers.isEmpty()) {
    log.debug("발견된 이슈 번호: {}", allIssueNumbers);
    githubIssueService.syncIssues(allIssueNumbers);
  } else {
    log.warn("컨트롤러에서 API 변경 이력을 찾을 수 없습니다");
  }
}
}
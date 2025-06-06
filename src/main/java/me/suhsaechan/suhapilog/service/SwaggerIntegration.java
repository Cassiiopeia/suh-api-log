package me.suhsaechan.suhapilog.service;

import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import me.suhsaechan.suhapilog.config.SuhApiLogAutoConfiguration;
import me.suhsaechan.suhapilog.config.SuhApiLogger;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.web.method.HandlerMethod;

/**
 * Swagger와의 통합을 위한 클래스
 * 이 클래스는 Swagger/SpringDoc이 존재할 때만 실제로 작동하도록 설계됨
 */
public class SwaggerIntegration {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(SuhApiLogAutoConfiguration.class);

  private final GithubIssueService githubIssueService;

  public SwaggerIntegration(GithubIssueService githubIssueService) {
    this.githubIssueService = githubIssueService;
  }

  /**
   * Swagger Operation 객체를 커스터마이즈
   * 이 메서드는 리플렉션을 통해 간접적으로 호출됨 (Swagger/SpringDoc 의존성이 있을 때만)
   */
  public Object customizeOperation(Object operation, HandlerMethod handlerMethod) {
    log.debug("SwaggerIntegration.customizeOperation 호출: {}", handlerMethod.getMethod().getName());
    try {
      // Swagger 의존성 확인
      Class<?> operationClass = operation.getClass();
      java.lang.reflect.Method descriptionGetter = operationClass.getMethod("getDescription");
      java.lang.reflect.Method descriptionSetter = operationClass.getMethod("setDescription", String.class);

      // 기존 설명 가져오기
      String originalDescription = (String) descriptionGetter.invoke(operation);
      if (originalDescription == null) {
        originalDescription = "";
      }

      // 인터페이스와 구현 클래스 모두에서 어노테이션 검색 (TYPE_HIERARCHY로 인터페이스까지 검색)
      MergedAnnotations annotations = MergedAnnotations.from(handlerMethod.getMethod(),
          MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);
      MergedAnnotation<ApiChangeLogs> apiChangeLogsAnnotation = annotations.get(ApiChangeLogs.class);

      if (apiChangeLogsAnnotation.isPresent()) {
        log.debug("API 변경 이력 어노테이션 발견: {}", handlerMethod.getMethod().getName());
        String tableHtml = generateApiChangeLogTable(apiChangeLogsAnnotation.synthesize().value());
        descriptionSetter.invoke(operation, originalDescription + tableHtml);
      } else {
        log.debug("API 변경 이력이 없음: {}", handlerMethod.getMethod().getName());
      }

      return operation;
    } catch (Exception e) {
      log.error("Swagger 통합 중 오류 발생: {}", e.getMessage(), e);
      return operation;
    }
  }

  /**
   * API 변경 이력 테이블 HTML 생성
   */
  private String generateApiChangeLogTable(ApiChangeLog[] apiChangeLogs) {
    StringBuilder tableBuilder = new StringBuilder();
    tableBuilder.append("\n\n**API 변경 이력:**\n")
        .append("<table>")
        .append("<thead>")
        .append("<tr>")
        .append("<th>날짜</th>")
        .append("<th>작성자</th>")
        .append("<th>이슈번호</th>")
        .append("<th>이슈 제목</th>")
        .append("<th>변경 내용</th>")
        .append("</tr>")
        .append("</thead>")
        .append("<tbody>");

    for (ApiChangeLog apiChangeLog : apiChangeLogs) {
      String description = apiChangeLog.description()
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;");

      String issueNumberCell = "";
      String issueTitleCell = "";

      if (apiChangeLog.issueNumber() > 0) {
        String issueUrl = githubIssueService.formatIssueBaseUrl(
            githubIssueService.getIssueBaseUrl(),
            apiChangeLog.issueNumber());
        issueNumberCell = String.format("<a href=\"%s\" target=\"_blank\">#%d</a>",
            issueUrl, apiChangeLog.issueNumber());

        try {
          issueTitleCell = githubIssueService.getOrFetchIssue(apiChangeLog.issueNumber()).getCleanTitle();
          // HTML 엔티티 이스케이프
          issueTitleCell = issueTitleCell
              .replace("&", "&amp;")
              .replace("<", "&lt;")
              .replace(">", "&gt;");
        } catch (Exception e) {
          issueTitleCell = "로딩 실패";
          log.error("이슈 제목 로딩 실패: {}", e.getMessage());
        }
      }

      tableBuilder.append(String.format(
          "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
          apiChangeLog.date(),
          apiChangeLog.author(),
          issueNumberCell,
          issueTitleCell,
          description));
    }

    tableBuilder.append("</tbody>").append("</table>");
    return tableBuilder.toString();
  }
}
package me.suhsaechan.suhapilog.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import me.suhsaechan.suhapilog.annotation.ApiLog;
import me.suhsaechan.suhapilog.config.ApiChangeLogProperties;
import me.suhsaechan.suhapilog.util.SuhApiLogger;
import me.suhsaechan.suhapilog.model.GithubIssue;
import me.suhsaechan.suhapilog.storage.IssueRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * GitHub 이슈 정보를 관리하는 서비스
 */
public class GithubIssueService {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(GithubIssueService.class);

  private final IssueRepository issueRepository;
  private final String issueBaseUrl;
  private final String scanPackage;
  private final ApiChangeLogProperties properties;

  public GithubIssueService(IssueRepository issueRepository, String issueBaseUrl, String scanPackage, ApiChangeLogProperties properties) {
    this.issueRepository = issueRepository;
    this.issueBaseUrl = issueBaseUrl;
    this.scanPackage = scanPackage;
    this.properties = properties;
  }

  public IssueRepository getIssueRepository() {
    return issueRepository;
  }

  public String getIssueBaseUrl() {
    return issueBaseUrl;
  }

  public String getScanPackage() {
    return scanPackage;
  }

  public ApiChangeLogProperties getProperties() {
    return properties;
  }

  /**
   * 이슈 번호에 해당하는 정보 조회 (캐시에 없으면 GitHub에서 가져옴)
   */
  public GithubIssue getOrFetchIssue(Integer issueNumber) {
    Optional<GithubIssue> maybeIssue = issueRepository.findByIssueNumber(issueNumber);
    if (maybeIssue.isPresent()) {
      log.debug("캐시에서 이슈 {} 조회 성공", issueNumber);
      return maybeIssue.get();
    } else {
      try {
        return fetchAndSaveIssue(issueNumber);
      } catch (Exception e) {
        log.error("이슈 {} 파싱 실패: {}", issueNumber, e.getMessage());
        throw new RuntimeException("이슈 " + issueNumber + " 파싱 실패", e);
      }
    }
  }

  /**
   * GitHub에서 이슈 정보를 가져와 저장
   */
  protected GithubIssue fetchAndSaveIssue(Integer issueNumber) throws IOException {

    // URL 유연 처리
    String issueUrl = formatIssueBaseUrl(issueBaseUrl, issueNumber);

    log.debug("이슈 {} 정보를 GitHub 에서 가져옵니다: {}", issueNumber, issueUrl);

    // GitHub 페이지에서 이슈 제목 파싱
    Document doc = Jsoup.connect(issueUrl).get();
    String title = doc.select("title").text();

    if (title == null || title.isEmpty()) {
      title = "Unknown Title";
      log.warn("이슈 {} 제목을 찾을 수 없습니다", issueNumber);
    }

    String cleanTitle = processIssueTitle(title);

    GithubIssue newIssue = GithubIssue.builder()
        .issueNumber(issueNumber)
        .rawTitle(title)
        .cleanTitle(cleanTitle)
        .pageUrl(issueUrl)
        .build();

    log.debug("새로운 이슈 {} 파싱 완료: {}", issueNumber, cleanTitle);
    return issueRepository.save(newIssue);
  }

  /**
   * 이슈 제목에서 GitHub 메타데이터 제거
   */
  private String processIssueTitle(String title) {
    if (title == null || title.isEmpty()) {
      return "Unknown Title";
    }

    // GitHub 메타정보 제거: "· Issue"를 기준으로 앞쪽 부분만 사용
    int issueIndex = title.indexOf("· Issue");
    if (issueIndex != -1) {
      title = title.substring(0, issueIndex).trim();
    }

    // 대괄호([ ... ])와 내부 내용 제거
    title = title.replaceAll("\\[[^\\]]*\\]", "").trim();

    // 앞쪽에 남아 있는 이모지나 기타 기호 제거
    title = title.replaceAll("^[^\\p{L}\\p{N}]+", "").trim();

    // 불필요한 중복 공백 제거
    title = title.replaceAll("\\s{2,}", " ");

    return title.isEmpty() ? "Unknown Title" : title;
  }

  /**
   * 지정된 클래스를 스캔하여 변경 로그 데이터를 생성
   */
  public String generateChangelogHtml(Method method) {
    if (method == null) {
      return "";
    }

    // getAnnotationsByType은 @Repeatable 단일/복수 모두 처리
    ApiLog[] apiLogs = method.getAnnotationsByType(ApiLog.class);
    if (apiLogs.length == 0) {
      return "";
    }

    StringBuilder tableBuilder = new StringBuilder();
    tableBuilder.append("\n\n**변경 관리 이력:**\n")
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

    for (ApiLog apiLog : apiLogs) {
      String description = escapeHtml(apiLog.description());
      String issueNumberCell = "";
      String issueTitleCell = "";
      String author = apiLog.author();
      if (author == null || author.isBlank()) {
        author = properties.getDefaultAuthor();
      }

      if (apiLog.issueNumber() > 0) {
        String issueUrl = formatIssueBaseUrl(issueBaseUrl, apiLog.issueNumber());
        issueNumberCell = String.format("<a href=\"%s\" target=\"_blank\">#%d</a>",
            issueUrl, apiLog.issueNumber());

        try {
          GithubIssue issue = getOrFetchIssue(apiLog.issueNumber());
          issueTitleCell = escapeHtml(issue.getCleanTitle());
        } catch (Exception e) {
          log.error("이슈 정보를 가져오는 데 실패했습니다: {}", e.getMessage());
          description = "이슈 정보 로드 실패: " + e.getMessage();
          issueTitleCell = "ERROR";
        }
      }

      tableBuilder.append(String.format(
          "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
          apiLog.date(),
          escapeHtml(apiLog.author()),
          issueNumberCell,
          issueTitleCell,
          description));
    }

    tableBuilder.append("</tbody>")
        .append("</table>");

    return tableBuilder.toString();
  }

  /**
   * 문자열에서 HTML 특수문자를 이스케이프 처리
   */
  public String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  /**
   * 이슈 정보 동기화 (GitHub에서 가져오기)
   */
  public void syncIssues(Set<Integer> issueNumbers) {
    if (issueNumbers.isEmpty()) {
      return;
    }

    // 필요한 이슈 정보만 가져오기
    List<GithubIssue> updatedIssues = new ArrayList<>();
    for (Integer issueNumber : issueNumbers) {
      try {
        updatedIssues.add(getOrFetchIssue(issueNumber));
      } catch (Exception e) {
        log.error("이슈 {} 동기화 실패: {}", issueNumber, e.getMessage());
      }
    }

    // 저장소에 저장
    issueRepository.saveAll(issueNumbers, updatedIssues);
  }

  // URL 구성 유연하게 처리
  public String formatIssueBaseUrl(String issueBaseUrl, Integer issueNumber) {
    // 기본 URL 정규화
    String normalizedUrl = issueBaseUrl.replaceAll("/+$", ""); // 끝의 슬래시 제거

    // /issues 경로가 없으면 추가
    if (!normalizedUrl.contains("/issues")) {
      normalizedUrl += "/issues";
    }

    return normalizedUrl + "/" + issueNumber;
  }
}
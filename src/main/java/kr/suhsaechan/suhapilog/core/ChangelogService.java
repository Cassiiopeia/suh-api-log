package kr.suhsaechan.suhapilog.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.fetch.JsoupIssueFetcher;
import kr.suhsaechan.suhapilog.spi.ApiLogEntry;
import kr.suhsaechan.suhapilog.spi.ChangelogRenderer;
import kr.suhsaechan.suhapilog.spi.IssueRef;

/**
 * &#64;ApiLog 어노테이션을 렌더링 가능한 엔트리로 정규화한다.
 *
 * <p>author 기본값(default-author) 폴백 적용 지점은 여기 한 곳
 */
public class ChangelogService {

  private final IssueResolver issueResolver;
  private final ChangelogRenderer renderer;
  private final String issueBaseUrl;
  private final String defaultAuthor;

  public ChangelogService(IssueResolver issueResolver, ChangelogRenderer renderer,
      String issueBaseUrl, String defaultAuthor) {
    this.issueResolver = issueResolver;
    this.renderer = renderer;
    this.issueBaseUrl = issueBaseUrl;
    this.defaultAuthor = (defaultAuthor == null) ? "" : defaultAuthor;
  }

  public List<ApiLogEntry> toEntries(Method method) {
    ApiLog[] apiLogs = ApiLogAnnotations.find(method);
    if (apiLogs.length == 0) {
      return List.of();
    }

    List<ApiLogEntry> entries = new ArrayList<>(apiLogs.length);
    for (ApiLog apiLog : apiLogs) {
      entries.add(toEntry(apiLog));
    }
    return entries;
  }

  public String renderFor(Method method) {
    List<ApiLogEntry> entries = toEntries(method);
    return entries.isEmpty() ? "" : renderer.render(entries);
  }

  private ApiLogEntry toEntry(ApiLog apiLog) {
    String author = apiLog.author();
    if (author == null || author.isBlank()) {
      author = defaultAuthor;
    }

    int issueNumber = apiLog.issueNumber();
    if (issueNumber <= 0) {
      return new ApiLogEntry(apiLog.date(), author, apiLog.description(), issueNumber, "", "");
    }

    // 이슈 링크는 조회 성공 여부와 무관하게 만들 수 있다 — 제목만 비워둔다
    String issueUrl = hasBaseUrl()
        ? JsoupIssueFetcher.formatIssueUrl(issueBaseUrl, issueNumber)
        : "";
    String issueTitle = issueResolver.resolve(issueNumber)
        .map(IssueRef::cleanTitle)
        .orElse("");

    return new ApiLogEntry(
        apiLog.date(), author, apiLog.description(), issueNumber, issueUrl, issueTitle);
  }

  private boolean hasBaseUrl() {
    return issueBaseUrl != null && !issueBaseUrl.isBlank();
  }
}

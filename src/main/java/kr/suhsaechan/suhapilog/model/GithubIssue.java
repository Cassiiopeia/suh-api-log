package kr.suhsaechan.suhapilog.model;

import java.time.LocalDateTime;
import kr.suhsaechan.suhapilog.spi.IssueRef;

/**
 * 하위 호환용 shim.
 *
 * @deprecated 3.0.0부터 {@link IssueRef}로 대체되었다. 4.0.0에서 제거된다.
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class GithubIssue {

  private Integer issueNumber;
  private String cleanTitle;
  private String rawTitle;
  private String pageUrl;
  private LocalDateTime fetchedAt;

  public GithubIssue() {
  }

  public GithubIssue(Integer issueNumber, String cleanTitle, String rawTitle, String pageUrl,
      LocalDateTime fetchedAt) {
    this.issueNumber = issueNumber;
    this.cleanTitle = cleanTitle;
    this.rawTitle = rawTitle;
    this.pageUrl = pageUrl;
    this.fetchedAt = fetchedAt;
  }

  /** 새 SPI 타입으로 변환한다. */
  public IssueRef toIssueRef() {
    return new IssueRef(issueNumber, cleanTitle, rawTitle, pageUrl, fetchedAt);
  }

  public static GithubIssue from(IssueRef ref) {
    return new GithubIssue(ref.issueNumber(), ref.cleanTitle(), ref.rawTitle(),
        ref.pageUrl(), ref.fetchedAt());
  }

  public Integer getIssueNumber() {
    return issueNumber;
  }

  public void setIssueNumber(Integer issueNumber) {
    this.issueNumber = issueNumber;
  }

  public String getCleanTitle() {
    return cleanTitle;
  }

  public void setCleanTitle(String cleanTitle) {
    this.cleanTitle = cleanTitle;
  }

  public String getRawTitle() {
    return rawTitle;
  }

  public void setRawTitle(String rawTitle) {
    this.rawTitle = rawTitle;
  }

  public String getPageUrl() {
    return pageUrl;
  }

  public void setPageUrl(String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public LocalDateTime getFetchedAt() {
    return fetchedAt;
  }

  public void setFetchedAt(LocalDateTime fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Integer issueNumber;
    private String cleanTitle;
    private String rawTitle;
    private String pageUrl;
    private LocalDateTime fetchedAt;

    public Builder issueNumber(Integer issueNumber) {
      this.issueNumber = issueNumber;
      return this;
    }

    public Builder cleanTitle(String cleanTitle) {
      this.cleanTitle = cleanTitle;
      return this;
    }

    public Builder rawTitle(String rawTitle) {
      this.rawTitle = rawTitle;
      return this;
    }

    public Builder pageUrl(String pageUrl) {
      this.pageUrl = pageUrl;
      return this;
    }

    public Builder fetchedAt(LocalDateTime fetchedAt) {
      this.fetchedAt = fetchedAt;
      return this;
    }

    public GithubIssue build() {
      return new GithubIssue(issueNumber, cleanTitle, rawTitle, pageUrl, fetchedAt);
    }
  }
}

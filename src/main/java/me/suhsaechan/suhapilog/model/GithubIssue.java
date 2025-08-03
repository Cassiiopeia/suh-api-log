package me.suhsaechan.suhapilog.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * GitHub 이슈 정보를 나타내는 모델 클래스
 */
public class GithubIssue implements Serializable {
  private static final long serialVersionUID = 1L;

  private Integer issueNumber;
  private String cleanTitle;
  private String rawTitle;
  private String pageUrl;
  private LocalDateTime fetchedAt;

  public GithubIssue() {
  }

  public GithubIssue(Integer issueNumber, String cleanTitle, String rawTitle, String pageUrl, LocalDateTime fetchedAt) {
    this.issueNumber = issueNumber;
    this.cleanTitle = cleanTitle;
    this.rawTitle = rawTitle;
    this.pageUrl = pageUrl;
    this.fetchedAt = fetchedAt;
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
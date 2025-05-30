package me.suhsaechan.suhapilog.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GitHub 이슈 정보를 나타내는 모델 클래스
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GithubIssue implements Serializable {
  private static final long serialVersionUID = 1L;

  private Integer issueNumber;
  private String cleanTitle;
  private String rawTitle;
  private String pageUrl;
  private LocalDateTime fetchedAt;
}
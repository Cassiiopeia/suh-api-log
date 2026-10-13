package kr.suhsaechan.suhapilog.spi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * 이슈 트래커에서 가져온 이슈 정보.
 *
 * @param issueNumber 이슈 번호
 * @param cleanTitle  정제된 제목 (표에 표시되는 값)
 * @param rawTitle    정제 전 원본 제목 (디버깅·재정제용)
 * @param pageUrl     이슈 페이지 URL
 * @param fetchedAt   조회 시각. cache.ttl 만료 판단에 쓰인다
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IssueRef(
    Integer issueNumber,
    String cleanTitle,
    String rawTitle,
    String pageUrl,
    LocalDateTime fetchedAt) {
}

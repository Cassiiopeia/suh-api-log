package kr.suhsaechan.suhapilog.spi;

import java.util.Optional;

/**
 * 이슈 조회 방식 확장점. 기본 구현은 GitHub 페이지를 스크래핑한다.
 *
 * <p>이 타입의 빈을 등록하면 기본 구현이 물러난다. GitLab·Jira·사내 트래커 연동에 쓴다.
 */
public interface IssueFetcher {

  /**
   * @return 조회 실패 시 {@link Optional#empty()}. 예외를 던져 상위 앱을 죽이지 않는다.
   */
  Optional<IssueRef> fetch(int issueNumber);
}

package kr.suhsaechan.suhapilog.spi;

import java.util.Collection;
import java.util.Optional;

/**
 * 이슈 캐시 저장소 확장점. 기본 구현은 JSON 파일이다.
 *
 * <p>이 타입의 빈을 등록하면 기본 구현이 물러난다. Redis·DB·인메모리 캐시에 쓴다.
 */
public interface IssueRepository {

  Optional<IssueRef> findByIssueNumber(int issueNumber);

  IssueRef save(IssueRef issue);

  Collection<IssueRef> findAll();

  /** 여러 건을 한 번에 저장한다. 구현체는 쓰기를 1회로 묶어야 한다. */
  void saveAll(Collection<IssueRef> issues);

  /** 마지막으로 동기화한 이슈 번호 집합의 해시. 없으면 빈 문자열. */
  String getCurrentHash();

  void updateHash(String hash);
}

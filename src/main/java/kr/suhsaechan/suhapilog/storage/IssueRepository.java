package kr.suhsaechan.suhapilog.storage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import kr.suhsaechan.suhapilog.model.GithubIssue;

/**
 * GitHub 이슈 정보를 저장하고 조회하는 리포지토리 인터페이스
 */
public interface IssueRepository {
  /**
   * 이슈 번호로 이슈 조회
   */
  Optional<GithubIssue> findByIssueNumber(Integer issueNumber);

  /**
   * 이슈 저장
   */
  GithubIssue save(GithubIssue issue);

  /**
   * 모든 이슈 조회
   */
  Collection<GithubIssue> findAll();

  /**
   * 여러 이슈 저장 및 해시값 업데이트
   */
  void saveAll(Set<Integer> issueNumbers, Collection<GithubIssue> issues);

  /**
   * 현재 저장된 이슈 목록 해시값 조회
   */
  String getCurrentHash();
}
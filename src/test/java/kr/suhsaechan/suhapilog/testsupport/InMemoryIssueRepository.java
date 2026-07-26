package kr.suhsaechan.suhapilog.testsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.IssueRepository;

/**
 * 파일 IO 없이 저장소 동작만 흉내내는 테스트 픽스처.
 * 여러 패키지의 테스트가 공유하므로 별도 클래스로 둔다.
 */
public class InMemoryIssueRepository implements IssueRepository {

  public final Map<Integer, IssueRef> store = new ConcurrentHashMap<>();
  private String hash = "";

  @Override
  public Optional<IssueRef> findByIssueNumber(int issueNumber) {
    return Optional.ofNullable(store.get(issueNumber));
  }

  @Override
  public IssueRef save(IssueRef issue) {
    store.put(issue.issueNumber(), issue);
    return issue;
  }

  @Override
  public Collection<IssueRef> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public void saveAll(Collection<IssueRef> issues) {
    issues.forEach(issue -> store.put(issue.issueNumber(), issue));
  }

  @Override
  public String getCurrentHash() {
    return hash;
  }

  @Override
  public void updateHash(String hash) {
    this.hash = hash;
  }
}

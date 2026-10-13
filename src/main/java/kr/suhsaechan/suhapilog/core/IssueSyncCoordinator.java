package kr.suhsaechan.suhapilog.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.IssueRepository;

/**
 * 부팅 시 이슈 캐시를 워밍업한다.
 *
 * <p>레거시는 해시를 계산해 저장만 하고 아무도 읽지 않아, 변경이 없어도 매 기동마다
 * 전량을 재조회했다. 이제 해시가 같고 캐시가 온전하면 네트워크 요청을 0회로 만든다.
 */
public class IssueSyncCoordinator {

  private static final SuhLogger log = SuhLog.getLogger(IssueSyncCoordinator.class);

  private final IssueResolver issueResolver;
  private final IssueRepository repository;
  private final int concurrency;

  public IssueSyncCoordinator(IssueResolver issueResolver, IssueRepository repository,
      int concurrency) {
    this.issueResolver = issueResolver;
    this.repository = repository;
    this.concurrency = concurrency;
  }

  /**
   * @return 실제로 조회를 수행했으면 true, 스킵했거나 대상이 없으면 false
   */
  public boolean sync(Class<?>... sources) {
    Set<Integer> issueNumbers = ControllerScanner.collectIssueNumbers(sources);
    if (issueNumbers.isEmpty()) {
      log.debug("컨트롤러에서 @ApiLog 이슈 번호를 찾지 못했습니다");
      return false;
    }

    String hash = hashOf(issueNumbers);
    if (!hash.isEmpty() && hash.equals(repository.getCurrentHash())
        && isCacheComplete(issueNumbers)) {
      log.debug("이슈 목록이 이전과 동일하고 캐시가 온전해 동기화를 건너뜁니다 ({}건)",
          issueNumbers.size());
      return false;
    }

    log.info("API 변경 이력 이슈 {}건 동기화를 시작합니다", issueNumbers.size());
    List<IssueRef> resolved = issueResolver.resolveAll(issueNumbers, concurrency);
    if (!resolved.isEmpty()) {
      repository.saveAll(resolved);
    }
    repository.updateHash(hash);
    log.info("API 변경 이력 동기화 완료 ({}/{}건 성공)", resolved.size(), issueNumbers.size());
    return true;
  }

  /** 해시가 같아도 캐시 파일이 지워졌을 수 있으므로 실제 보유 여부를 확인한다. */
  private boolean isCacheComplete(Collection<Integer> issueNumbers) {
    for (Integer number : issueNumbers) {
      if (repository.findByIssueNumber(number).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /** 이슈 번호 집합의 SHA-256. 입력 순서와 무관하게 동일한 값을 낸다. */
  public static String hashOf(Collection<Integer> issueNumbers) {
    if (issueNumbers == null || issueNumbers.isEmpty()) {
      return "";
    }
    List<Integer> sorted = new ArrayList<>(issueNumbers);
    sorted.sort(Integer::compareTo);

    StringBuilder joined = new StringBuilder();
    for (Integer number : sorted) {
      joined.append(number).append(',');
    }

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(joined.toString().getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(bytes);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256은 JDK 표준이라 실제로는 도달하지 않는다. 도달해도 앱을 죽이지 않는다.
      log.warn("해시 계산에 실패해 동기화 스킵 최적화를 사용하지 않습니다: {}", e.getMessage());
      return "";
    }
  }
}

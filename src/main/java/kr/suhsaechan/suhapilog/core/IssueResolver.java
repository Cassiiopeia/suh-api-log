package kr.suhsaechan.suhapilog.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.IssueRepository;

/**
 * 캐시 우선 이슈 해석기.
 *
 * <p>캐시에 있으면 그대로 쓰고, 없거나 TTL이 지났으면 fetcher로 조회한 뒤 저장한다.
 * 어떤 실패에도 예외를 전파하지 않는다 — 문서화 보조 기능이 상위 앱을 죽이면 안 된다.
 */
public class IssueResolver {

  private static final SuhLogger log = SuhLog.getLogger(IssueResolver.class);

  private final IssueRepository repository;
  private final IssueFetcher fetcher;
  private final Duration cacheTtl;

  /** @param cacheTtl null이거나 0 이하면 캐시를 무기한 사용한다 */
  public IssueResolver(IssueRepository repository, IssueFetcher fetcher, Duration cacheTtl) {
    this.repository = repository;
    this.fetcher = fetcher;
    this.cacheTtl = cacheTtl;
  }

  public Optional<IssueRef> resolve(int issueNumber) {
    Optional<IssueRef> cached = repository.findByIssueNumber(issueNumber);
    if (cached.isPresent() && isFresh(cached.get())) {
      return cached;
    }

    Optional<IssueRef> fetched = fetcher.fetch(issueNumber);
    if (fetched.isPresent()) {
      repository.save(fetched.get());
      return fetched;
    }

    // 조회에 실패해도 만료된 캐시가 있으면 그거라도 보여주는 게 빈칸보다 낫다
    return cached;
  }

  /**
   * 여러 이슈를 병렬로 해석한다.
   *
   * @param concurrency 1 이하면 순차 처리
   * @return 해석에 성공한 것만. 순서는 보장하지 않는다
   */
  public List<IssueRef> resolveAll(Collection<Integer> issueNumbers, int concurrency) {
    if (issueNumbers == null || issueNumbers.isEmpty()) {
      return List.of();
    }

    if (concurrency <= 1) {
      List<IssueRef> results = new ArrayList<>();
      for (Integer number : issueNumbers) {
        resolve(number).ifPresent(results::add);
      }
      return results;
    }

    int poolSize = Math.min(concurrency, issueNumbers.size());
    ExecutorService pool = Executors.newFixedThreadPool(poolSize, runnable -> {
      Thread thread = new Thread(runnable, "suh-api-log-issue-fetch");
      thread.setDaemon(true); // 조회가 늦어져도 JVM 종료를 막지 않는다
      return thread;
    });

    try {
      List<Callable<Optional<IssueRef>>> tasks = new ArrayList<>();
      for (Integer number : issueNumbers) {
        tasks.add(() -> resolve(number));
      }

      List<IssueRef> results = new ArrayList<>();
      for (Future<Optional<IssueRef>> future : pool.invokeAll(tasks)) {
        try {
          future.get().ifPresent(results::add);
        } catch (Exception e) {
          log.debug("이슈 해석 중 오류: {}", e.getMessage());
        }
      }
      return results;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return List.of();
    } finally {
      pool.shutdown();
      try {
        pool.awaitTermination(1, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private boolean isFresh(IssueRef issue) {
    if (cacheTtl == null || cacheTtl.isZero() || cacheTtl.isNegative()) {
      return true; // TTL 미설정 = 무기한 유효
    }
    LocalDateTime fetchedAt = issue.fetchedAt();
    if (fetchedAt == null) {
      return false; // 3.0.0 이전 캐시에는 조회 시각이 없다 — 갱신 대상으로 본다
    }
    return fetchedAt.plus(cacheTtl).isAfter(LocalDateTime.now());
  }
}

package kr.suhsaechan.suhapilog.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.core.IssueResolverTest.InMemoryRepository;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import org.junit.jupiter.api.Test;

class IssueSyncCoordinatorTest {

  static class Controller {
    @ApiLog(date = "2026-07-27", author = "A", description = "d", issueNumber = 17)
    public void a() {
    }

    @ApiLog(date = "2026-07-27", author = "A", description = "d", issueNumber = 18)
    public void b() {
    }
  }

  static class NoAnnotation {
    public void x() {
    }
  }

  private static class Counter implements IssueFetcher {
    final AtomicInteger calls = new AtomicInteger();

    @Override
    public Optional<IssueRef> fetch(int n) {
      calls.incrementAndGet();
      return Optional.of(new IssueRef(n, "제목", "raw", "url", LocalDateTime.now()));
    }
  }

  private IssueSyncCoordinator coordinator(InMemoryRepository repo, Counter fetcher) {
    return new IssueSyncCoordinator(new IssueResolver(repo, fetcher, null), repo, 4);
  }

  @Test
  void 최초_동기화는_이슈를_조회하고_해시를_기록한다() {
    InMemoryRepository repo = new InMemoryRepository();
    Counter fetcher = new Counter();

    assertThat(coordinator(repo, fetcher).sync(Controller.class)).isTrue();

    assertThat(fetcher.calls.get()).isEqualTo(2);
    assertThat(repo.findAll()).hasSize(2);
    assertThat(repo.getCurrentHash()).isNotEmpty();
  }

  @Test
  void 해시가_같고_캐시가_있으면_네트워크_조회를_건너뛴다() {
    InMemoryRepository repo = new InMemoryRepository();
    Counter fetcher = new Counter();
    IssueSyncCoordinator coordinator = coordinator(repo, fetcher);

    coordinator.sync(Controller.class);
    int afterFirst = fetcher.calls.get();

    assertThat(coordinator.sync(Controller.class)).isFalse(); // 스킵되었음을 알린다
    assertThat(fetcher.calls.get()).isEqualTo(afterFirst);
  }

  @Test
  void 해시가_같아도_캐시가_비면_다시_조회한다() {
    InMemoryRepository repo = new InMemoryRepository();
    Counter fetcher = new Counter();
    IssueSyncCoordinator coordinator = coordinator(repo, fetcher);

    coordinator.sync(Controller.class);
    repo.store.clear(); // 캐시 파일이 지워진 상황

    assertThat(coordinator.sync(Controller.class)).isTrue();
    assertThat(fetcher.calls.get()).isEqualTo(4);
  }

  @Test
  void 어노테이션이_없으면_아무것도_하지_않는다() {
    InMemoryRepository repo = new InMemoryRepository();
    Counter fetcher = new Counter();

    assertThat(coordinator(repo, fetcher).sync(NoAnnotation.class)).isFalse();
    assertThat(fetcher.calls.get()).isZero();
  }

  @Test
  void 해시는_순서와_무관하게_같은_집합에_대해_동일하다() {
    assertThat(IssueSyncCoordinator.hashOf(List.of(3, 1, 2)))
        .isEqualTo(IssueSyncCoordinator.hashOf(Set.of(1, 2, 3)));
    assertThat(IssueSyncCoordinator.hashOf(List.of(1, 2)))
        .isNotEqualTo(IssueSyncCoordinator.hashOf(List.of(1, 2, 3)));
    assertThat(IssueSyncCoordinator.hashOf(List.of())).isEmpty();
    assertThat(IssueSyncCoordinator.hashOf(null)).isEmpty();
  }
}

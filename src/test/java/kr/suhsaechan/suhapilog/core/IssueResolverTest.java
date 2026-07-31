package kr.suhsaechan.suhapilog.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.testsupport.InMemoryIssueRepository;
import org.junit.jupiter.api.Test;

class IssueResolverTest {

  static class CountingFetcher implements IssueFetcher {
    final AtomicInteger calls = new AtomicInteger();
    private final boolean succeed;

    CountingFetcher(boolean succeed) {
      this.succeed = succeed;
    }

    @Override
    public Optional<IssueRef> fetch(int issueNumber) {
      calls.incrementAndGet();
      return succeed
          ? Optional.of(new IssueRef(issueNumber, "제목" + issueNumber, "raw", "url",
              LocalDateTime.now()))
          : Optional.empty();
    }
  }

  @Test
  void 캐시에_있으면_조회하지_않는다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    repo.save(new IssueRef(17, "캐시된 제목", "raw", "url", LocalDateTime.now()));
    CountingFetcher fetcher = new CountingFetcher(true);

    IssueRef ref = new IssueResolver(repo, fetcher, null).resolve(17).orElseThrow();

    assertThat(ref.cleanTitle()).isEqualTo("캐시된 제목");
    assertThat(fetcher.calls.get()).isZero();
  }

  @Test
  void 캐시에_없으면_조회하고_저장한다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    CountingFetcher fetcher = new CountingFetcher(true);

    IssueRef ref = new IssueResolver(repo, fetcher, null).resolve(17).orElseThrow();

    assertThat(ref.cleanTitle()).isEqualTo("제목17");
    assertThat(fetcher.calls.get()).isEqualTo(1);
    assertThat(repo.findByIssueNumber(17)).isPresent();
  }

  @Test
  void TTL이_지난_캐시는_다시_조회한다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    repo.save(new IssueRef(17, "오래된 제목", "raw", "url", LocalDateTime.now().minusDays(2)));
    CountingFetcher fetcher = new CountingFetcher(true);

    IssueRef ref =
        new IssueResolver(repo, fetcher, Duration.ofHours(1)).resolve(17).orElseThrow();

    assertThat(ref.cleanTitle()).isEqualTo("제목17");
    assertThat(fetcher.calls.get()).isEqualTo(1);
  }

  @Test
  void TTL이_null이면_캐시를_무기한_사용한다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    repo.save(new IssueRef(17, "오래된 제목", "raw", "url", LocalDateTime.now().minusYears(5)));
    CountingFetcher fetcher = new CountingFetcher(true);

    assertThat(new IssueResolver(repo, fetcher, null).resolve(17).orElseThrow().cleanTitle())
        .isEqualTo("오래된 제목");
    assertThat(fetcher.calls.get()).isZero();
  }

  @Test
  void 조회에_실패하면_빈값을_반환하고_예외를_던지지_않는다() {
    IssueResolver resolver =
        new IssueResolver(new InMemoryIssueRepository(), new CountingFetcher(false), null);

    assertThat(resolver.resolve(17)).isEmpty();
  }

  @Test
  void 조회에_실패해도_만료된_캐시가_있으면_그것을_돌려준다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    repo.save(new IssueRef(17, "만료된 제목", "raw", "url", LocalDateTime.now().minusDays(2)));

    IssueRef ref = new IssueResolver(repo, new CountingFetcher(false), Duration.ofHours(1))
        .resolve(17).orElseThrow();

    assertThat(ref.cleanTitle()).isEqualTo("만료된 제목");
  }

  @Test
  void resolveAll은_병렬로_조회하고_성공한_것만_돌려준다() {
    InMemoryIssueRepository repo = new InMemoryIssueRepository();
    CountingFetcher fetcher = new CountingFetcher(true);

    List<IssueRef> refs =
        new IssueResolver(repo, fetcher, null).resolveAll(Set.of(1, 2, 3, 4, 5), 4);

    assertThat(refs).hasSize(5);
    assertThat(fetcher.calls.get()).isEqualTo(5);
  }

  @Test
  void resolveAll은_동시성이_1이하면_순차_처리한다() {
    CountingFetcher fetcher = new CountingFetcher(true);

    assertThat(new IssueResolver(new InMemoryIssueRepository(), fetcher, null)
        .resolveAll(Set.of(1, 2, 3), 1)).hasSize(3);
    assertThat(fetcher.calls.get()).isEqualTo(3);
  }

  @Test
  void resolveAll은_빈_입력에_대해_조회하지_않는다() {
    CountingFetcher fetcher = new CountingFetcher(true);

    assertThat(new IssueResolver(new InMemoryIssueRepository(), fetcher, null)
        .resolveAll(Set.of(), 4)).isEmpty();
    assertThat(fetcher.calls.get()).isZero();
  }
}

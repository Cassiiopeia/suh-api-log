package kr.suhsaechan.suhapilog.fetch;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import kr.suhsaechan.suhapilog.fetch.JsoupIssueFetcher.Options;
import kr.suhsaechan.suhapilog.render.DefaultTitleNormalizer;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import org.junit.jupiter.api.Test;

class JsoupIssueFetcherTest {

  private static final String BASE = "https://github.com/Cassiiopeia/suh-api-log/issues/";

  /** 네트워크 대신 미리 정해둔 응답/예외를 돌려주는 테스트 더블 */
  private static class StubFetcher extends JsoupIssueFetcher {
    final AtomicInteger calls = new AtomicInteger();
    private final String title;
    private final boolean fail;

    StubFetcher(String title, boolean fail, Options options) {
      super(BASE, new DefaultTitleNormalizer(), options);
      this.title = title;
      this.fail = fail;
    }

    @Override
    protected String fetchRawTitle(String url) throws IOException {
      calls.incrementAndGet();
      if (fail) {
        throw new IOException("연결 실패");
      }
      return title;
    }
  }

  private Options fastOptions(int maxAttempts, Duration negativeTtl) {
    return new Options(Duration.ofSeconds(1), maxAttempts, Duration.ZERO,
        "suh-api-log/test", negativeTtl);
  }

  @Test
  void 이슈_URL을_정규화한다() {
    assertThat(JsoupIssueFetcher.formatIssueUrl("https://github.com/o/r/issues/", 17))
        .isEqualTo("https://github.com/o/r/issues/17");
    assertThat(JsoupIssueFetcher.formatIssueUrl("https://github.com/o/r", 17))
        .isEqualTo("https://github.com/o/r/issues/17");
    assertThat(JsoupIssueFetcher.formatIssueUrl("https://github.com/o/r///", 17))
        .isEqualTo("https://github.com/o/r/issues/17");
  }

  @Test
  void 조회에_성공하면_정제된_제목과_조회시각을_채운다() {
    StubFetcher fetcher = new StubFetcher(
        "🚀 [기능개선] 로깅 위임 · Issue #17 · Cassiiopeia/suh-api-log", false,
        fastOptions(2, Duration.ofMinutes(10)));

    IssueRef ref = fetcher.fetch(17).orElseThrow();

    assertThat(ref.issueNumber()).isEqualTo(17);
    assertThat(ref.cleanTitle()).isEqualTo("로깅 위임");
    assertThat(ref.rawTitle()).contains("Issue #17");
    assertThat(ref.pageUrl()).isEqualTo(BASE + "17");
    assertThat(ref.fetchedAt()).isNotNull(); // 레거시는 이 값을 한 번도 채우지 않았다
  }

  @Test
  void 실패하면_설정한_횟수만큼_재시도하고_빈값을_반환한다() {
    StubFetcher fetcher = new StubFetcher(null, true, fastOptions(3, Duration.ofMinutes(10)));

    assertThat(fetcher.fetch(17)).isEmpty();
    assertThat(fetcher.calls.get()).isEqualTo(3);
  }

  @Test
  void 실패한_이슈는_negative_TTL_동안_재요청하지_않는다() {
    StubFetcher fetcher = new StubFetcher(null, true, fastOptions(1, Duration.ofMinutes(10)));

    fetcher.fetch(17);
    fetcher.fetch(17);
    fetcher.fetch(17);

    assertThat(fetcher.calls.get()).isEqualTo(1); // 2·3회차는 캐시된 실패로 즉시 반환
  }

  @Test
  void negative_TTL이_0이면_매번_재시도한다() {
    StubFetcher fetcher = new StubFetcher(null, true, fastOptions(1, Duration.ZERO));

    fetcher.fetch(17);
    fetcher.fetch(17);

    assertThat(fetcher.calls.get()).isEqualTo(2);
  }

  @Test
  void 빈_제목이_와도_예외를_던지지_않는다() {
    StubFetcher fetcher = new StubFetcher("", false, fastOptions(1, Duration.ofMinutes(10)));

    IssueRef ref = fetcher.fetch(17).orElseThrow();

    assertThat(ref.cleanTitle()).isEqualTo("Unknown Title");
  }

  @Test
  void base_url이_비면_네트워크에_나가지_않고_빈값을_반환한다() {
    JsoupIssueFetcher fetcher =
        new JsoupIssueFetcher("", new DefaultTitleNormalizer(), Options.defaults());

    assertThat(fetcher.fetch(17)).isEmpty();
  }

  @Test
  void 기본_옵션은_5초_타임아웃과_2회_시도를_쓴다() {
    Options defaults = Options.defaults();

    assertThat(defaults.timeout()).isEqualTo(Duration.ofSeconds(5));
    assertThat(defaults.maxAttempts()).isEqualTo(2);
    assertThat(defaults.userAgent()).startsWith("suh-api-log/");
    assertThat(defaults.negativeTtl()).isEqualTo(Duration.ofMinutes(10));
  }

  @Test
  void 여러_이슈를_동시에_조회해도_안전하다() throws Exception {
    StubFetcher fetcher = new StubFetcher("제목 · Issue #1 · o/r", false,
        fastOptions(1, Duration.ofMinutes(10)));

    List<Thread> threads = List.of(
        new Thread(() -> fetcher.fetch(1)),
        new Thread(() -> fetcher.fetch(2)),
        new Thread(() -> fetcher.fetch(3)));
    threads.forEach(Thread::start);
    for (Thread t : threads) {
      t.join(5000);
    }

    assertThat(fetcher.calls.get()).isEqualTo(3);
  }
}

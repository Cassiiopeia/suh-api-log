package kr.suhsaechan.suhapilog.fetch;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.TitleNormalizer;
import kr.suhsaechan.suhapilog.util.SuhApiLogVersion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * GitHub 이슈 페이지의 title을 스크래핑하는 기본 조회 구현.
 *
 * <p>레거시 대비 달라진 점:
 * <ul>
 *   <li>timeout 설정 — 기존에는 무제한이라 GitHub가 느리면 부팅이 매달렸다</li>
 *   <li>재시도 + 백오프</li>
 *   <li>negative cache — 실패한 이슈를 Swagger 열 때마다 재요청하던 문제 차단</li>
 *   <li>예외를 던지지 않고 Optional.empty() 반환 — 상위 앱을 죽이지 않는다</li>
 * </ul>
 */
public class JsoupIssueFetcher implements IssueFetcher {

  private static final SuhLogger log = SuhLog.getLogger(JsoupIssueFetcher.class);

  /**
   * @param timeout     jsoup은 connect/read를 분리하지 않고 총 요청 시간 하나만 받는다
   * @param maxAttempts 최초 시도를 포함한 총 시도 횟수 (1이면 재시도 없음)
   * @param negativeTtl 실패를 기억하는 시간. Duration.ZERO면 negative cache를 쓰지 않는다
   */
  public record Options(
      Duration timeout,
      int maxAttempts,
      Duration backoff,
      String userAgent,
      Duration negativeTtl) {

    public static Options defaults() {
      return new Options(
          Duration.ofSeconds(5),
          2,
          Duration.ofMillis(500),
          "suh-api-log/" + SuhApiLogVersion.get(),
          Duration.ofMinutes(10));
    }
  }

  private final String baseUrl;
  private final TitleNormalizer titleNormalizer;
  private final Options options;
  private final Map<Integer, Instant> failedAt = new ConcurrentHashMap<>();

  public JsoupIssueFetcher(String baseUrl, TitleNormalizer titleNormalizer, Options options) {
    this.baseUrl = baseUrl;
    this.titleNormalizer = titleNormalizer;
    this.options = options;
  }

  @Override
  public Optional<IssueRef> fetch(int issueNumber) {
    if (baseUrl == null || baseUrl.isBlank()) {
      return Optional.empty(); // base-url 미설정 시 이슈 연동만 비활성화된다
    }
    if (isNegativelyCached(issueNumber)) {
      log.debug("이슈 {} 는 최근 조회에 실패해 재요청을 건너뜁니다", issueNumber);
      return Optional.empty();
    }

    String url = formatIssueUrl(baseUrl, issueNumber);
    int attempts = Math.max(1, options.maxAttempts());

    for (int attempt = 1; attempt <= attempts; attempt++) {
      try {
        String rawTitle = fetchRawTitle(url);
        failedAt.remove(issueNumber);
        return Optional.of(new IssueRef(
            issueNumber,
            titleNormalizer.normalize(rawTitle),
            rawTitle,
            url,
            LocalDateTime.now()));
      } catch (Exception e) {
        log.debug("이슈 {} 조회 {}/{} 실패: {}", issueNumber, attempt, attempts, e.getMessage());
        if (attempt < attempts) {
          sleepBackoff();
        }
      }
    }

    failedAt.put(issueNumber, Instant.now());
    log.warn("이슈 {} 조회에 실패했습니다: {}", issueNumber, url);
    return Optional.empty();
  }

  /** 네트워크 경계. 테스트에서 오버라이드한다. */
  protected String fetchRawTitle(String url) throws IOException {
    Document doc = Jsoup.connect(url)
        .userAgent(options.userAgent())
        .timeout((int) options.timeout().toMillis())
        .get();
    return doc.select("title").text();
  }

  private boolean isNegativelyCached(int issueNumber) {
    Duration ttl = options.negativeTtl();
    if (ttl == null || ttl.isZero() || ttl.isNegative()) {
      return false;
    }
    Instant at = failedAt.get(issueNumber);
    if (at == null) {
      return false;
    }
    if (Instant.now().isBefore(at.plus(ttl))) {
      return true;
    }
    failedAt.remove(issueNumber); // TTL이 지났으면 다시 시도할 기회를 준다
    return false;
  }

  private void sleepBackoff() {
    Duration backoff = options.backoff();
    if (backoff == null || backoff.isZero() || backoff.isNegative()) {
      return;
    }
    try {
      Thread.sleep(backoff.toMillis());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** 끝 슬래시를 정리하고 /issues 경로가 없으면 붙인다. */
  public static String formatIssueUrl(String baseUrl, int issueNumber) {
    String normalized = baseUrl.replaceAll("/+$", "");
    if (!normalized.contains("/issues")) {
      normalized += "/issues";
    }
    return normalized + "/" + issueNumber;
  }
}

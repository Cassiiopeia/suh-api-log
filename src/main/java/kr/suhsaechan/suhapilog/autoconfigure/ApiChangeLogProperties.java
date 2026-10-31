package kr.suhsaechan.suhapilog.autoconfigure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * suh-api-log 전체 설정.
 *
 * <p>아무것도 설정하지 않아도 기본값만으로 동작한다. 특히 로깅은 미설정 시
 * 상위 프로젝트의 log4j2/logback 설정을 그대로 따른다.
 */
@ConfigurationProperties(prefix = "suhapilog")
public class ApiChangeLogProperties {

  /** 라이브러리 전체 활성화 여부 */
  private boolean enabled = true;

  /** &#64;ApiLog의 author가 비었을 때 사용할 기본 작성자 */
  private String defaultAuthor = "";

  private Logging logging = new Logging();
  private Github github = new Github();
  private Storage storage = new Storage();
  private Render render = new Render();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDefaultAuthor() {
    return defaultAuthor;
  }

  public void setDefaultAuthor(String defaultAuthor) {
    this.defaultAuthor = defaultAuthor;
  }

  public Logging getLogging() {
    return logging;
  }

  public void setLogging(Logging logging) {
    this.logging = logging;
  }

  public Github getGithub() {
    return github;
  }

  public void setGithub(Github github) {
    this.github = github;
  }

  public Storage getStorage() {
    return storage;
  }

  public void setStorage(Storage storage) {
    this.storage = storage;
  }

  public Render getRender() {
    return render;
  }

  public void setRender(Render render) {
    this.render = render;
  }

  /** 부팅 시 이슈 동기화 방식 */
  public enum StartupMode {
    /** 동기화가 끝날 때까지 부팅을 막는다 (2.x 동작) */
    BLOCKING,
    /** 백그라운드로 워밍업한다. 그 사이 요청은 지연 조회가 커버한다 (기본값) */
    ASYNC,
    /** 부팅 시 아무것도 하지 않고 Swagger 렌더 시점에만 조회한다 */
    LAZY
  }

  /**
   * 로깅 오버라이드. 전부 미설정이면 상위 프로젝트 설정을 그대로 따른다.
   *
   * <p>실제 적용은 {@link SuhApiLogEnvironmentPostProcessor}가 담당한다 — 정적 로거는
   * 빈 생성보다 먼저 만들어지기 때문이다. 이 클래스는 IDE 자동완성과 문서화를 위한 선언이다.
   */
  public static class Logging {

    /** false면 이 라이브러리가 찍는 로그를 전면 차단한다 */
    private boolean enabled = true;

    /**
     * 라이브러리 로그의 하한 레벨(trace/debug/info/warn/error).
     * 상위가 허용한 범위를 좁힐 수만 있고, 상위가 막은 레벨을 열지는 못한다.
     */
    private String level;

    /** auto(기본)/slf4j/log4j2/jul/none */
    private String backend = "auto";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getLevel() {
      return level;
    }

    public void setLevel(String level) {
      this.level = level;
    }

    public String getBackend() {
      return backend;
    }

    public void setBackend(String backend) {
      this.backend = backend;
    }
  }

  public static class Github {

    /** 이슈 기본 URL. 미설정이면 이슈 연동만 비활성화되고 나머지는 동작한다 */
    private String baseUrl;

    /** 이슈 페이지 조회 총 타임아웃 (jsoup은 connect/read를 분리하지 않는다) */
    private Duration timeout = Duration.ofSeconds(5);

    /** 스크래핑 차단 회피용 User-Agent. 미설정이면 suh-api-log/{version} */
    private String userAgent;

    /** 부팅 시 병렬 조회 스레드 수 */
    private int concurrency = 4;

    private StartupMode startupMode = StartupMode.ASYNC;

    /** true면 base-url 미설정 시 예외를 던져 부팅을 중단한다 (2.x 동작) */
    private boolean failFast = false;

    private Retry retry = new Retry();
    private Cache cache = new Cache();

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
      return timeout;
    }

    public void setTimeout(Duration timeout) {
      this.timeout = timeout;
    }

    public String getUserAgent() {
      return userAgent;
    }

    public void setUserAgent(String userAgent) {
      this.userAgent = userAgent;
    }

    public int getConcurrency() {
      return concurrency;
    }

    public void setConcurrency(int concurrency) {
      this.concurrency = concurrency;
    }

    public StartupMode getStartupMode() {
      return startupMode;
    }

    public void setStartupMode(StartupMode startupMode) {
      this.startupMode = startupMode;
    }

    public boolean isFailFast() {
      return failFast;
    }

    public void setFailFast(boolean failFast) {
      this.failFast = failFast;
    }

    public Retry getRetry() {
      return retry;
    }

    public void setRetry(Retry retry) {
      this.retry = retry;
    }

    public Cache getCache() {
      return cache;
    }

    public void setCache(Cache cache) {
      this.cache = cache;
    }

    public static class Retry {

      /** 최초 시도를 포함한 총 시도 횟수 */
      private int maxAttempts = 2;

      private Duration backoff = Duration.ofMillis(500);

      public int getMaxAttempts() {
        return maxAttempts;
      }

      public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
      }

      public Duration getBackoff() {
        return backoff;
      }

      public void setBackoff(Duration backoff) {
        this.backoff = backoff;
      }
    }

    public static class Cache {

      /** 캐시 유효 기간. 미설정이면 무기한 */
      private Duration ttl;

      /** 조회 실패를 기억하는 시간. Swagger 열 때마다 재요청하는 것을 막는다 */
      private Duration negativeTtl = Duration.ofMinutes(10);

      public Duration getTtl() {
        return ttl;
      }

      public void setTtl(Duration ttl) {
        this.ttl = ttl;
      }

      public Duration getNegativeTtl() {
        return negativeTtl;
      }

      public void setNegativeTtl(Duration negativeTtl) {
        this.negativeTtl = negativeTtl;
      }
    }
  }

  public static class Storage {

    /** 이슈 캐시 저장 경로. 상대 경로면 프로젝트 루트 기준으로 해석된다 */
    private String path = "./github-issues";

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

  public static class Render {

    /** 표시할 컬럼과 순서 (csv) */
    private String columns = "date,author,issue-number,issue-title,description";

    /** none / date-asc / date-desc */
    private String sort = "date-desc";

    /** 표 위에 붙는 제목 */
    private String title = "API 변경 이력";

    private boolean escapeHtml = true;

    public String getColumns() {
      return columns;
    }

    public void setColumns(String columns) {
      this.columns = columns;
    }

    public String getSort() {
      return sort;
    }

    public void setSort(String sort) {
      this.sort = sort;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public boolean isEscapeHtml() {
      return escapeHtml;
    }

    public void setEscapeHtml(boolean escapeHtml) {
      this.escapeHtml = escapeHtml;
    }
  }
}

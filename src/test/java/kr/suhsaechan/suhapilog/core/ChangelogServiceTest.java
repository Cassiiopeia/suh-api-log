package kr.suhsaechan.suhapilog.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.testsupport.InMemoryIssueRepository;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer.Options;
import kr.suhsaechan.suhapilog.spi.ApiLogEntry;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import org.junit.jupiter.api.Test;

class ChangelogServiceTest {

  private static final String BASE = "https://github.com/o/r/issues/";

  static class Controller {

    @ApiLog(date = "2026-07-27", author = "", description = "작성자 비움", issueNumber = 17)
    public void blankAuthor() {
    }

    @ApiLog(date = "2026-07-27", author = "서새찬", description = "작성자 명시", issueNumber = 17)
    public void namedAuthor() {
    }

    @ApiLog(date = "2026-07-27", author = "A", description = "이슈 없음")
    public void noIssue() {
    }

    public void plain() {
    }
  }

  private ChangelogService service(String defaultAuthor, boolean fetchSucceeds) {
    IssueFetcher fetcher = number -> fetchSucceeds
        ? Optional.of(new IssueRef(number, "로깅 위임", "raw", BASE + number, LocalDateTime.now()))
        : Optional.empty();
    IssueResolver resolver = new IssueResolver(new InMemoryIssueRepository(), fetcher, null);
    return new ChangelogService(resolver,
        new HtmlTableChangelogRenderer(Options.defaults()), BASE, defaultAuthor);
  }

  @Test
  void author가_비면_defaultAuthor로_대체한다() throws Exception {
    // 레거시 회귀: GithubIssueService.java:160 에서 폴백을 계산만 하고 쓰지 않았다
    Method method = Controller.class.getMethod("blankAuthor");

    List<ApiLogEntry> entries = service("기본작성자", true).toEntries(method);

    assertThat(entries).hasSize(1);
    assertThat(entries.get(0).author()).isEqualTo("기본작성자");
  }

  @Test
  void author가_있으면_그대로_쓴다() throws Exception {
    Method method = Controller.class.getMethod("namedAuthor");

    assertThat(service("기본작성자", true).toEntries(method).get(0).author())
        .isEqualTo("서새찬");
  }

  @Test
  void 이슈_제목과_URL을_채운다() throws Exception {
    ApiLogEntry entry =
        service("", true).toEntries(Controller.class.getMethod("namedAuthor")).get(0);

    assertThat(entry.issueNumber()).isEqualTo(17);
    assertThat(entry.issueUrl()).isEqualTo(BASE + "17");
    assertThat(entry.issueTitle()).isEqualTo("로깅 위임");
  }

  @Test
  void 이슈_조회에_실패해도_나머지_정보는_렌더링한다() throws Exception {
    ApiLogEntry entry =
        service("", false).toEntries(Controller.class.getMethod("namedAuthor")).get(0);

    assertThat(entry.description()).isEqualTo("작성자 명시");
    assertThat(entry.issueUrl()).isEqualTo(BASE + "17"); // 링크는 여전히 만들 수 있다
    assertThat(entry.issueTitle()).isEmpty();
  }

  @Test
  void 이슈번호가_없으면_URL과_제목을_비운다() throws Exception {
    ApiLogEntry entry = service("", true).toEntries(Controller.class.getMethod("noIssue")).get(0);

    assertThat(entry.issueNumber()).isLessThanOrEqualTo(0);
    assertThat(entry.issueUrl()).isEmpty();
    assertThat(entry.issueTitle()).isEmpty();
  }

  @Test
  void 어노테이션이_없으면_빈_결과와_빈_문자열을_반환한다() throws Exception {
    Method plain = Controller.class.getMethod("plain");

    assertThat(service("", true).toEntries(plain)).isEmpty();
    assertThat(service("", true).renderFor(plain)).isEmpty();
    assertThat(service("", true).renderFor(null)).isEmpty();
  }

  @Test
  void renderFor는_렌더러_결과를_그대로_돌려준다() throws Exception {
    String html = service("", true).renderFor(Controller.class.getMethod("namedAuthor"));

    assertThat(html).contains("<table>").contains("로깅 위임").contains("#17");
  }
}

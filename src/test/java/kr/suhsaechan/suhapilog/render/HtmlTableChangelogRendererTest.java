package kr.suhsaechan.suhapilog.render;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer.Column;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer.Options;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer.Sort;
import kr.suhsaechan.suhapilog.spi.ApiLogEntry;
import org.junit.jupiter.api.Test;

class HtmlTableChangelogRendererTest {

  private ApiLogEntry entry(String date, String desc) {
    return new ApiLogEntry(date, "서새찬", desc, 17,
        "https://github.com/o/r/issues/17", "로깅 위임");
  }

  @Test
  void 엔트리가_없으면_빈_문자열을_반환한다() {
    assertThat(new HtmlTableChangelogRenderer(Options.defaults()).render(List.of())).isEmpty();
    assertThat(new HtmlTableChangelogRenderer(Options.defaults()).render(null)).isEmpty();
  }

  @Test
  void 기본_옵션은_5개_컬럼_헤더를_모두_출력한다() {
    String html = new HtmlTableChangelogRenderer(Options.defaults())
        .render(List.of(entry("2026-07-27", "설명")));

    assertThat(html)
        .contains("<th>날짜</th>", "<th>작성자</th>", "<th>이슈번호</th>",
            "<th>이슈 제목</th>", "<th>변경 내용</th>")
        .contains("API 변경 이력")
        .contains("<a href=\"https://github.com/o/r/issues/17\" target=\"_blank\">#17</a>");
  }

  @Test
  void 컬럼을_선택하고_순서를_바꿀_수_있다() {
    Options options = new Options(
        List.of(Column.DESCRIPTION, Column.DATE), Sort.NONE, "변경 이력", true);

    String html = new HtmlTableChangelogRenderer(options)
        .render(List.of(entry("2026-07-27", "설명")));

    assertThat(html).contains("<th>변경 내용</th><th>날짜</th>");
    assertThat(html).doesNotContain("<th>작성자</th>");
    assertThat(html).contains("변경 이력");
  }

  @Test
  void 기본_정렬은_날짜_내림차순이다() {
    String html = new HtmlTableChangelogRenderer(Options.defaults()).render(List.of(
        entry("2026-01-01", "오래된 변경"),
        entry("2026-07-27", "최신 변경")));

    assertThat(html.indexOf("최신 변경")).isLessThan(html.indexOf("오래된 변경"));
  }

  @Test
  void 오름차순과_정렬_없음을_지원한다() {
    List<ApiLogEntry> entries = List.of(
        entry("2026-07-27", "최신 변경"), entry("2026-01-01", "오래된 변경"));

    String asc = new HtmlTableChangelogRenderer(
        new Options(Column.defaultColumns(), Sort.DATE_ASC, "t", true)).render(entries);
    assertThat(asc.indexOf("오래된 변경")).isLessThan(asc.indexOf("최신 변경"));

    String none = new HtmlTableChangelogRenderer(
        new Options(Column.defaultColumns(), Sort.NONE, "t", true)).render(entries);
    assertThat(none.indexOf("최신 변경")).isLessThan(none.indexOf("오래된 변경"));
  }

  @Test
  void HTML_특수문자_다섯_개를_모두_이스케이프한다() {
    ApiLogEntry entry = new ApiLogEntry("2026-07-27", "a&b",
        "<script>alert('x')</script>", 17, "https://github.com/o/r/issues/17", "\"제목\"");

    String html = new HtmlTableChangelogRenderer(Options.defaults()).render(List.of(entry));

    assertThat(html)
        .contains("a&amp;b")
        .contains("&lt;script&gt;")
        .contains("&#39;")
        .contains("&quot;제목&quot;")
        .doesNotContain("<script>");
  }

  @Test
  void escapeHtml을_끄면_원문을_그대로_출력한다() {
    Options options = new Options(Column.defaultColumns(), Sort.NONE, "t", false);

    String html = new HtmlTableChangelogRenderer(options)
        .render(List.of(entry("2026-07-27", "<b>강조</b>")));

    assertThat(html).contains("<b>강조</b>");
  }

  @Test
  void 이슈번호가_0_이하면_이슈_칸을_비운다() {
    ApiLogEntry entry = new ApiLogEntry("2026-07-27", "서새찬", "설명", -1, "", "");

    String html = new HtmlTableChangelogRenderer(Options.defaults()).render(List.of(entry));

    assertThat(html).doesNotContain("<a href");
    assertThat(html).contains("<td></td>");
  }

  @Test
  void 컬럼_CSV와_정렬_문자열을_파싱한다() {
    assertThat(Column.parse("date,issue-number"))
        .containsExactly(Column.DATE, Column.ISSUE_NUMBER);
    assertThat(Column.parse("없는컬럼")).isEqualTo(Column.defaultColumns());
    assertThat(Column.parse(null)).isEqualTo(Column.defaultColumns());
    assertThat(Sort.parseOrDefault("date-asc")).isEqualTo(Sort.DATE_ASC);
    assertThat(Sort.parseOrDefault("없는값")).isEqualTo(Sort.DATE_DESC);
    assertThat(Sort.parseOrDefault(null)).isEqualTo(Sort.DATE_DESC);
  }
}

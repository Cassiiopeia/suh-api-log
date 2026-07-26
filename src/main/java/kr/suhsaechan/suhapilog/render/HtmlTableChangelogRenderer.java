package kr.suhsaechan.suhapilog.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import kr.suhsaechan.suhapilog.spi.ApiLogEntry;
import kr.suhsaechan.suhapilog.spi.ChangelogRenderer;

/**
 * 변경 이력을 HTML 표로 렌더링하는 기본 구현.
 *
 * <p>레거시에서는 이 로직이 GithubIssueService와 SwaggerIntegration에 두 벌 있었고
 * 이스케이프 규칙마저 달랐다(한쪽은 따옴표를 막지 않았다). 한 벌로 통합한다.
 */
public class HtmlTableChangelogRenderer implements ChangelogRenderer {

  public enum Column {
    DATE("date", "날짜"),
    AUTHOR("author", "작성자"),
    ISSUE_NUMBER("issue-number", "이슈번호"),
    ISSUE_TITLE("issue-title", "이슈 제목"),
    DESCRIPTION("description", "변경 내용");

    private final String key;
    private final String header;

    Column(String key, String header) {
      this.key = key;
      this.header = header;
    }

    public String header() {
      return header;
    }

    /** CSV 파싱. 유효한 값이 하나도 없으면 기본 컬럼 전체를 돌려준다. */
    public static List<Column> parse(String csv) {
      if (csv == null || csv.isBlank()) {
        return defaultColumns();
      }
      List<Column> parsed = new ArrayList<>();
      for (String token : csv.split(",")) {
        String key = token.trim().toLowerCase();
        for (Column column : values()) {
          if (column.key.equals(key) && !parsed.contains(column)) {
            parsed.add(column);
          }
        }
      }
      return parsed.isEmpty() ? defaultColumns() : List.copyOf(parsed);
    }

    public static List<Column> defaultColumns() {
      return List.of(values());
    }
  }

  public enum Sort {
    NONE, DATE_ASC, DATE_DESC;

    public static Sort parseOrDefault(String value) {
      if (value == null || value.isBlank()) {
        return DATE_DESC;
      }
      String normalized = value.trim().toUpperCase().replace('-', '_');
      return Arrays.stream(values())
          .filter(s -> s.name().equals(normalized))
          .findFirst()
          .orElse(DATE_DESC);
    }
  }

  public record Options(List<Column> columns, Sort sort, String title, boolean escapeHtml) {

    public static Options defaults() {
      return new Options(Column.defaultColumns(), Sort.DATE_DESC, "API 변경 이력", true);
    }
  }

  private final Options options;

  public HtmlTableChangelogRenderer(Options options) {
    this.options = options;
  }

  @Override
  public String render(List<ApiLogEntry> entries) {
    if (entries == null || entries.isEmpty()) {
      return "";
    }

    List<ApiLogEntry> ordered = sorted(entries);
    List<Column> columns = options.columns();

    StringBuilder html = new StringBuilder();
    html.append("\n\n**").append(options.title()).append(":**\n")
        .append("<table><thead><tr>");
    for (Column column : columns) {
      html.append("<th>").append(column.header()).append("</th>");
    }
    html.append("</tr></thead><tbody>");

    for (ApiLogEntry entry : ordered) {
      html.append("<tr>");
      for (Column column : columns) {
        html.append("<td>").append(cell(entry, column)).append("</td>");
      }
      html.append("</tr>");
    }

    return html.append("</tbody></table>").toString();
  }

  private List<ApiLogEntry> sorted(List<ApiLogEntry> entries) {
    if (options.sort() == Sort.NONE) {
      return entries;
    }
    // date는 문자열이지만 YYYY-MM-DD 권장 포맷이라 사전순 == 시간순이다.
    // 포맷이 어긋난 값도 예외 없이 정렬되도록 null-safe 비교자를 쓴다.
    Comparator<ApiLogEntry> byDate =
        Comparator.comparing(e -> e.date() == null ? "" : e.date());
    List<ApiLogEntry> copy = new ArrayList<>(entries);
    copy.sort(options.sort() == Sort.DATE_ASC ? byDate : byDate.reversed());
    return copy;
  }

  private String cell(ApiLogEntry entry, Column column) {
    return switch (column) {
      case DATE -> escape(entry.date());
      case AUTHOR -> escape(entry.author());
      case DESCRIPTION -> escape(entry.description());
      case ISSUE_TITLE -> entry.issueNumber() > 0 ? escape(entry.issueTitle()) : "";
      case ISSUE_NUMBER -> issueLink(entry);
    };
  }

  private String issueLink(ApiLogEntry entry) {
    if (entry.issueNumber() <= 0 || entry.issueUrl() == null || entry.issueUrl().isEmpty()) {
      return "";
    }
    // URL은 설정값 + 정수로 조립되므로 속성 인젝션 여지가 없지만, 방어적으로 이스케이프한다
    return "<a href=\"" + escapeAlways(entry.issueUrl()) + "\" target=\"_blank\">#"
        + entry.issueNumber() + "</a>";
  }

  private String escape(String text) {
    return options.escapeHtml() ? escapeAlways(text) : (text == null ? "" : text);
  }

  /** &amp;, &lt;, &gt;, ", ' 다섯 문자를 모두 처리한다. */
  private static String escapeAlways(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

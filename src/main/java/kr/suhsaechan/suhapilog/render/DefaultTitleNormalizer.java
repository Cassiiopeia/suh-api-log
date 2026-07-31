package kr.suhsaechan.suhapilog.render;

import java.util.regex.Pattern;
import kr.suhsaechan.suhapilog.spi.TitleNormalizer;

/**
 * GitHub 페이지 title에서 메타정보를 걷어내는 기본 정제기.
 *
 * <p>정규식은 호출마다 재컴파일하지 않도록 상수로 올려둔다 — Swagger 렌더링 시
 * 이슈 개수만큼 반복 호출된다.
 */
public class DefaultTitleNormalizer implements TitleNormalizer {

  private static final String FALLBACK = "Unknown Title";
  private static final String GITHUB_META_MARKER = "· Issue";
  private static final Pattern BRACKET_TAG = Pattern.compile("\\[[^\\]]*\\]");
  private static final Pattern LEADING_SYMBOLS = Pattern.compile("^[^\\p{L}\\p{N}]+");
  private static final Pattern REPEATED_SPACE = Pattern.compile("\\s{2,}");

  @Override
  public String normalize(String rawTitle) {
    if (rawTitle == null || rawTitle.isEmpty()) {
      return FALLBACK;
    }

    String title = rawTitle;

    // "제목 · Issue #17 · owner/repo" 에서 앞부분만 취한다
    int metaIndex = title.indexOf(GITHUB_META_MARKER);
    if (metaIndex != -1) {
      title = title.substring(0, metaIndex).trim();
    }

    title = BRACKET_TAG.matcher(title).replaceAll("").trim();
    title = LEADING_SYMBOLS.matcher(title).replaceAll("").trim();
    title = REPEATED_SPACE.matcher(title).replaceAll(" ").trim();

    return title.isEmpty() ? FALLBACK : title;
  }
}

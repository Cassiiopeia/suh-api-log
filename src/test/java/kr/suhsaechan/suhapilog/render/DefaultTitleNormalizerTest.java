package kr.suhsaechan.suhapilog.render;

import static org.assertj.core.api.Assertions.assertThat;

import kr.suhsaechan.suhapilog.spi.TitleNormalizer;
import org.junit.jupiter.api.Test;

class DefaultTitleNormalizerTest {

  private final TitleNormalizer normalizer = new DefaultTitleNormalizer();

  @Test
  void GitHub_메타정보를_제거한다() {
    assertThat(normalizer.normalize("로깅 개선 · Issue #17 · Cassiiopeia/suh-api-log"))
        .isEqualTo("로깅 개선");
  }

  @Test
  void 대괄호_태그를_제거한다() {
    assertThat(normalizer.normalize("[기능개선][로깅] 상위 설정 위임"))
        .isEqualTo("상위 설정 위임");
  }

  @Test
  void 앞쪽_이모지와_기호를_제거한다() {
    assertThat(normalizer.normalize("🚀 상위 설정 위임")).isEqualTo("상위 설정 위임");
  }

  @Test
  void 중복_공백을_한_칸으로_줄인다() {
    assertThat(normalizer.normalize("상위    설정   위임")).isEqualTo("상위 설정 위임");
  }

  @Test
  void 정제_결과가_비면_Unknown_Title을_반환한다() {
    assertThat(normalizer.normalize("[전부][태그]")).isEqualTo("Unknown Title");
    assertThat(normalizer.normalize("")).isEqualTo("Unknown Title");
    assertThat(normalizer.normalize(null)).isEqualTo("Unknown Title");
  }

  @Test
  void 실제_이슈_제목_전체를_정제한다() {
    String raw = "🚀 [기능개선][전체] 레거시 전면 개선 · Issue #17 · Cassiiopeia/suh-api-log";
    assertThat(normalizer.normalize(raw)).isEqualTo("레거시 전면 개선");
  }
}

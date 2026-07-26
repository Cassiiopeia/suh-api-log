package kr.suhsaechan.suhapilog.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JulMessageFormatterTest {

  @Test
  void 플레이스홀더를_순서대로_치환한다() {
    assertThat(JulMessageFormatter.format("이슈 {} 조회 {}", 17, "성공"))
        .isEqualTo("이슈 17 조회 성공");
  }

  @Test
  void 인자가_부족하면_플레이스홀더를_그대로_남긴다() {
    assertThat(JulMessageFormatter.format("{} / {}", 1)).isEqualTo("1 / {}");
  }

  @Test
  void 이스케이프된_플레이스홀더는_치환하지_않는다() {
    assertThat(JulMessageFormatter.format("\\{} 는 {}", "리터럴")).isEqualTo("{} 는 리터럴");
  }

  @Test
  void null_인자와_null_메시지를_안전하게_처리한다() {
    assertThat(JulMessageFormatter.format("값: {}", (Object) null)).isEqualTo("값: null");
    assertThat(JulMessageFormatter.format(null)).isEqualTo("null");
  }

  @Test
  void 배열_인자를_읽을_수_있게_펼친다() {
    assertThat(JulMessageFormatter.format("{}", (Object) new int[] {1, 2})).isEqualTo("[1, 2]");
    assertThat(JulMessageFormatter.format("{}", (Object) new String[] {"a", "b"}))
        .isEqualTo("[a, b]");
  }

  @Test
  void 인자가_없으면_원본을_그대로_반환한다() {
    assertThat(JulMessageFormatter.format("변경 없음")).isEqualTo("변경 없음");
  }
}

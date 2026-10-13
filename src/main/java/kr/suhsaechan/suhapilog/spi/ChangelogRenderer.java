package kr.suhsaechan.suhapilog.spi;

import java.util.List;

/**
 * 변경 이력 출력 확장점. 기본 구현은 HTML 표를 만든다.
 *
 * <p>이 타입의 빈을 등록하면 기본 구현이 물러난다. 카드형·타임라인형 렌더링에 쓴다.
 */
public interface ChangelogRenderer {

  /**
   * @param entries 비어 있을 수 있다. 그 경우 빈 문자열을 반환해야 한다.
   * @return Swagger description 뒤에 이어붙일 문자열
   */
  String render(List<ApiLogEntry> entries);
}

package kr.suhsaechan.suhapilog.spi;

/**
 * 이슈 제목 정제 규칙 확장점.
 *
 * <p>이 타입의 빈을 등록하면 기본 구현이 물러난다. 팀별 이슈 제목 컨벤션에 맞춘다.
 */
public interface TitleNormalizer {

  /**
   * @param rawTitle null이거나 빈 문자열일 수 있다
   * @return 절대 null을 반환하지 않는다. 정제 결과가 비면 대체 문자열을 반환한다
   */
  String normalize(String rawTitle);
}

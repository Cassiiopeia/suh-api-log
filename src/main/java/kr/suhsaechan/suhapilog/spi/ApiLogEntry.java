package kr.suhsaechan.suhapilog.spi;

/**
 * 렌더러에 전달되는 정규화된 변경 이력 한 줄.
 *
 * <p>어노테이션을 그대로 넘기지 않는 이유는 렌더러 구현자가 @ApiLog API에 결합되지
 * 않게 하기 위해서다. author 기본값 폴백과 이슈 제목 조회는 이미 끝난 상태로 전달된다.
 *
 * @param issueNumber 0 이하이면 이슈 미지정 — 렌더러는 이슈 칸을 비워야 한다
 * @param issueUrl    이슈 미지정이면 빈 문자열
 * @param issueTitle  조회에 실패하면 빈 문자열
 */
public record ApiLogEntry(
    String date,
    String author,
    String description,
    int issueNumber,
    String issueUrl,
    String issueTitle) {
}

package me.suhsaechan.suhapilog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 변경 이력을 문서화하는 어노테이션
 *
 * @Repeatable 적용으로 중첩 없이 여러 개 사용 가능:
 * <pre>
 * {@code
 * @ApiLog(date = "2024-12-20", author = "A", description = "변경1")
 * @ApiLog(date = "2024-12-15", author = "B", description = "변경2")
 * public ResponseEntity<?> getUsers() { ... }
 * }
 * </pre>
 */
@Repeatable(ApiLogs.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiLog {
  /**
   * 변경 날짜 (YYYY-MM-DD 형식 권장)
   */
  String date();

  /**
   * 작성자 정보
   */
  String author();

  /**
   * 변경 사항 설명
   */
  String description();

  /**
   * GitHub 이슈 번호 (기본값 -1: 미지정, 양수값이면 이슈 링크 추가)
   */
  int issueNumber() default -1;
}

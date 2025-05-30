package me.suhsaechan.suhapilog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 변경 이력을 문서화하는 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiChangeLog {
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
package kr.suhsaechan.suhapilog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * &#64;ApiLog 반복 선언용 컨테이너 (&#64;Repeatable 자동 사용, 직접 선언 불필요)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiLogs {
  ApiLog[] value();
}

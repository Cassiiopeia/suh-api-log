package kr.suhsaechan.suhapilog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 여러 API 변경 이력을 그룹화하는 컨테이너 어노테이션
 *
 * @ApiLog에 @Repeatable이 적용되어 있으므로, 이 컨테이너는 자동으로 사용됩니다.
 * 명시적으로 사용할 필요가 없습니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiLogs {
  ApiLog[] value();
}

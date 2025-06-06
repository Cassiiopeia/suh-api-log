package me.suhsaechan.suhapilog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 여러 API 변경 이력을 그룹화하는 컨테이너 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiChangeLogs {
  ApiChangeLog[] value();
}
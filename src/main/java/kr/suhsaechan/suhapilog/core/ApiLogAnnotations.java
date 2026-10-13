package kr.suhsaechan.suhapilog.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.annotation.ApiLogs;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

/**
 * &#64;ApiLog 탐색을 한 곳으로 모은다.
 *
 * <p>레거시는 스캔 경로와 Swagger 경로가 서로 다른 방식(수동 인터페이스 순회 vs
 * MergedAnnotations)을 써서 결과가 어긋날 수 있었다. TYPE_HIERARCHY 한 가지로 통일한다.
 */
public final class ApiLogAnnotations {

  private static final ApiLog[] EMPTY = new ApiLog[0];

  private ApiLogAnnotations() {
  }

  /** 구현 클래스와 인터페이스 계층을 모두 뒤져 &#64;ApiLog를 찾는다. 없으면 빈 배열. */
  public static ApiLog[] find(Method method) {
    if (method == null) {
      return EMPTY;
    }

    MergedAnnotations annotations =
        MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY);

    // @Repeatable 컨테이너가 먼저다 — 여러 개 선언 시 @ApiLogs로 감싸진다
    MergedAnnotation<ApiLogs> container = annotations.get(ApiLogs.class);
    if (container.isPresent()) {
      return container.synthesize().value();
    }

    MergedAnnotation<ApiLog> single = annotations.get(ApiLog.class);
    if (single.isPresent()) {
      return new ApiLog[] {single.synthesize()};
    }

    return EMPTY;
  }

  /** issueNumber가 양수인 것만 추린다. */
  public static List<Integer> issueNumbersOf(ApiLog[] apiLogs) {
    List<Integer> numbers = new ArrayList<>();
    for (ApiLog apiLog : apiLogs) {
      if (apiLog.issueNumber() > 0) {
        numbers.add(apiLog.issueNumber());
      }
    }
    return numbers;
  }
}

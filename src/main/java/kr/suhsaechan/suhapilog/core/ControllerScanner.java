package kr.suhsaechan.suhapilog.core;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import org.springframework.util.ClassUtils;

/**
 * 컨트롤러 클래스에서 &#64;ApiLog의 이슈 번호를 수집한다.
 */
public final class ControllerScanner {

  private static final SuhLogger log = SuhLog.getLogger(ControllerScanner.class);

  private ControllerScanner() {
  }

  /** @return 정렬된 이슈 번호 집합. 해시 계산이 순서에 의존하므로 TreeSet을 쓴다. */
  public static Set<Integer> collectIssueNumbers(Class<?>... sources) {
    Set<Integer> issueNumbers = new TreeSet<>();
    if (sources == null) {
      return issueNumbers;
    }

    for (Class<?> source : sources) {
      if (source == null) {
        continue;
      }
      Class<?> target = unwrap(source);
      for (Method method : target.getMethods()) {
        ApiLog[] apiLogs = ApiLogAnnotations.find(method);
        if (apiLogs.length > 0) {
          issueNumbers.addAll(ApiLogAnnotations.issueNumbersOf(apiLogs));
        }
      }
    }

    log.debug("컨트롤러 {}개에서 이슈 번호 {}건 수집", sources.length, issueNumbers.size());
    return issueNumbers;
  }

  /**
   * CGLIB 프록시라면 원본 클래스를 돌려준다.
   * 레거시의 {@code getName().contains("$$")} 문자열 검사보다 정확하다.
   */
  public static Class<?> unwrap(Class<?> candidate) {
    return ClassUtils.getUserClass(candidate);
  }
}

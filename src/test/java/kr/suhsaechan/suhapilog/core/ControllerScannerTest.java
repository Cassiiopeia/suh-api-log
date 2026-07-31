package kr.suhsaechan.suhapilog.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Set;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.junit.jupiter.api.Test;

class ControllerScannerTest {

  interface UserApi {
    @ApiLog(date = "2026-01-01", author = "A", description = "인터페이스 선언", issueNumber = 11)
    void fromInterface();
  }

  static class UserController implements UserApi {

    @Override
    public void fromInterface() {
    }

    @ApiLog(date = "2026-02-01", author = "B", description = "단일", issueNumber = 22)
    public void single() {
    }

    @ApiLog(date = "2026-03-01", author = "C", description = "반복1", issueNumber = 33)
    @ApiLog(date = "2026-03-02", author = "C", description = "반복2", issueNumber = 44)
    public void repeated() {
    }

    @ApiLog(date = "2026-04-01", author = "D", description = "이슈 없음")
    public void noIssue() {
    }

    public void plain() {
    }
  }

  static class Empty {
    public void nothing() {
    }
  }

  @Test
  void 클래스와_인터페이스_양쪽의_이슈_번호를_모두_모은다() {
    Set<Integer> numbers = ControllerScanner.collectIssueNumbers(UserController.class);

    assertThat(numbers).containsExactlyInAnyOrder(11, 22, 33, 44);
  }

  @Test
  void issueNumber가_0_이하인_어노테이션은_제외한다() {
    assertThat(ControllerScanner.collectIssueNumbers(UserController.class))
        .doesNotContain(-1, 0);
  }

  @Test
  void 어노테이션이_없으면_빈_집합을_반환한다() {
    assertThat(ControllerScanner.collectIssueNumbers(Empty.class)).isEmpty();
    assertThat(ControllerScanner.collectIssueNumbers()).isEmpty();
    assertThat(ControllerScanner.collectIssueNumbers((Class<?>[]) null)).isEmpty();
  }

  @Test
  void 반복_어노테이션을_모두_찾는다() throws Exception {
    Method method = UserController.class.getMethod("repeated");

    assertThat(ApiLogAnnotations.find(method)).hasSize(2);
  }

  @Test
  void 인터페이스에_선언된_어노테이션을_구현_메서드에서_찾는다() throws Exception {
    Method method = UserController.class.getMethod("fromInterface");

    ApiLog[] logs = ApiLogAnnotations.find(method);

    assertThat(logs).hasSize(1);
    assertThat(logs[0].issueNumber()).isEqualTo(11);
  }

  @Test
  void 어노테이션이_없는_메서드는_빈_배열을_반환한다() throws Exception {
    assertThat(ApiLogAnnotations.find(UserController.class.getMethod("plain"))).isEmpty();
    assertThat(ApiLogAnnotations.find(null)).isEmpty();
  }

  @Test
  void CGLIB_프록시가_아닌_클래스는_그대로_반환한다() {
    // 실제 CGLIB 프록시는 "Xxx$$SpringCGLIB$$0" 형태이며 ClassUtils가 상위 클래스로 되돌린다
    assertThat(ControllerScanner.unwrap(UserController.class)).isEqualTo(UserController.class);
  }
}

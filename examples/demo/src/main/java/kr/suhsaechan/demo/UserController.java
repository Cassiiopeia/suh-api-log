package kr.suhsaechan.demo;

import java.util.List;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * README·문서 사이트 스크린샷용 예제 컨트롤러.
 * 이슈 번호는 이 레포(Cassiiopeia/suh-api-log)의 실제 이슈라 제목이 자동으로 채워진다.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  record User(Long id, String name) {
  }

  @ApiLog(date = "2026-07-31", author = "서새찬", issueNumber = 17, description = "응답 필드 정리 및 로깅 위임")
  @ApiLog(date = "2026-01-18", author = "서새찬", issueNumber = 13, description = "캐시 경로 버그 수정")
  @ApiLog(date = "2025-05-28", author = "서새찬", issueNumber = 1, description = "사용자 목록 조회 API 최초 구현")
  @GetMapping
  public List<User> getUsers() {
    return List.of(new User(1L, "suh"), new User(2L, "kim"));
  }

  @ApiLog(date = "2025-08-03", author = "김개발", issueNumber = 5, description = "단건 조회 API 추가")
  @GetMapping("/{id}")
  public User getUser(@PathVariable Long id) {
    return new User(id, "suh");
  }

  // 이슈 없이도 쓸 수 있다
  @ApiLog(date = "2026-01-18", author = "이설계", description = "요청 본문 검증 추가")
  @PostMapping
  public User createUser(@RequestBody User user) {
    return user;
  }
}

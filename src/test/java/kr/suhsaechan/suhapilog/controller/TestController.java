package kr.suhsaechan.suhapilog.controller;

import kr.suhsaechan.suhapilog.annotation.ApiLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  // @Repeatable 적용으로 단일 어노테이션도 간편하게 사용 가능
  @ApiLog(date = "2025-05-30", author = "서새찬", issueNumber = 1, description = "테스트 API 추가")
  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("TEST API SUCCESS");
  }

  // 여러 개 사용 시에도 중첩 없이 반복 가능
  @ApiLog(date = "2025-05-30", author = "서새찬", issueNumber = 3, description = "테스트 API2 추가")
  @ApiLog(date = "2025-05-30", author = "서새찬", issueNumber = 1, description = "테스트 API2 수정")
  @GetMapping("/test2")
  public ResponseEntity<String> test2() {
    return ResponseEntity.ok("TEST API SUCCESS");
  }
}

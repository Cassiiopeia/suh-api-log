package me.suhsaechan.suhapilog.controller;

import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-05-30",
          author = "서새찬",
          issueNumber = 1,
          description = "테스트 API 추가"
      )
  })
  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("TEST API SUCCESS");
  }

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025-05-30",
          author = "서새찬",
          issueNumber = 3,
          description = "테스트 API2 추가"
      ),
      @ApiChangeLog(
          date = "2025-05-30",
          author = "서새찬",
          issueNumber = 1,
          description = "테스트 API2 추가"
      )
  })
  @GetMapping("/test2")
  public ResponseEntity<String> test2() {
    return ResponseEntity.ok("TEST API SUCCESS");
  }
}

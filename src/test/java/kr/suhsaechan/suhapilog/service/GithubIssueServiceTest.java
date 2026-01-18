package kr.suhsaechan.suhapilog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GithubIssueServiceTest {

  @Autowired
  private GithubIssueService githubIssueService;

  @Test
  void mainTest() throws Exception {
    testApiWithChangelog();
  }

  void testApiWithChangelog() throws Exception {
    // 이슈 동기화 확인 (이슈 번호 1번)
    githubIssueService.getOrFetchIssue(1);
    githubIssueService.getOrFetchIssue(3);
  }

}
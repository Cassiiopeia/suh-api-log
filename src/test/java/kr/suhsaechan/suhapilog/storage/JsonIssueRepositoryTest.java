package kr.suhsaechan.suhapilog.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonIssueRepositoryTest {

  @TempDir
  Path tempDir;

  private IssueRef issue(int number) {
    return new IssueRef(number, "제목" + number, "raw" + number,
        "https://github.com/o/r/issues/" + number, LocalDateTime.now());
  }

  @Test
  void 저장한_이슈를_번호로_조회한다() {
    JsonIssueRepository repo = new JsonIssueRepository(tempDir.toString());
    repo.save(issue(17));

    assertThat(repo.findByIssueNumber(17)).isPresent()
        .get().extracting(IssueRef::cleanTitle).isEqualTo("제목17");
    assertThat(repo.findByIssueNumber(99)).isEmpty();
  }

  @Test
  void 새_인스턴스가_파일에서_이슈를_복원한다() {
    new JsonIssueRepository(tempDir.toString()).saveAll(List.of(issue(1), issue(2)));

    JsonIssueRepository reloaded = new JsonIssueRepository(tempDir.toString());

    assertThat(reloaded.findAll()).hasSize(2);
    assertThat(reloaded.findByIssueNumber(2)).isPresent();
  }

  @Test
  void fetchedAt을_보존한다() {
    LocalDateTime at = LocalDateTime.of(2026, 7, 27, 12, 0, 0);
    new JsonIssueRepository(tempDir.toString())
        .save(new IssueRef(5, "t", "r", "u", at));

    JsonIssueRepository reloaded = new JsonIssueRepository(tempDir.toString());

    assertThat(reloaded.findByIssueNumber(5)).get()
        .extracting(IssueRef::fetchedAt).isEqualTo(at);
  }

  @Test
  void 해시를_저장하고_복원한다() {
    JsonIssueRepository repo = new JsonIssueRepository(tempDir.toString());
    assertThat(repo.getCurrentHash()).isEmpty();

    repo.updateHash("abc123");

    assertThat(repo.getCurrentHash()).isEqualTo("abc123");
    assertThat(new JsonIssueRepository(tempDir.toString()).getCurrentHash()).isEqualTo("abc123");
  }

  @Test
  void 손상된_JSON_파일이_있어도_생성에_실패하지_않는다() throws IOException {
    Files.writeString(tempDir.resolve("github-issues.json"), "{깨진 JSON");

    JsonIssueRepository repo = new JsonIssueRepository(tempDir.toString());

    assertThat(repo.findAll()).isEmpty(); // 경고만 남기고 빈 상태로 시작한다
    repo.save(issue(1)); // 이후 저장은 정상 동작해야 한다
    assertThat(repo.findAll()).hasSize(1);
  }

  @Test
  void 동시_저장에도_JSON이_깨지지_않는다() throws Exception {
    JsonIssueRepository repo = new JsonIssueRepository(tempDir.toString());
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch start = new CountDownLatch(1);

    for (int i = 0; i < threads; i++) {
      final int n = i + 1;
      pool.submit(() -> {
        start.await();
        repo.save(issue(n));
        return null;
      });
    }
    start.countDown();
    pool.shutdown();
    assertThat(pool.awaitTermination(20, TimeUnit.SECONDS)).isTrue();

    // 파일이 유효한 JSON이고 8건 전부 복원되어야 한다
    assertThat(new JsonIssueRepository(tempDir.toString()).findAll()).hasSize(threads);
  }

  @Test
  void saveAll은_임시파일을_남기지_않고_원자적으로_교체한다() throws IOException {
    JsonIssueRepository repo = new JsonIssueRepository(tempDir.toString());
    Path file = tempDir.resolve("github-issues.json");

    repo.saveAll(List.of(issue(1), issue(2), issue(3)));

    assertThat(Files.exists(file)).isTrue();
    assertThat(new JsonIssueRepository(tempDir.toString()).findAll()).hasSize(3);
    try (var stream = Files.list(tempDir)) {
      assertThat(stream.map(p -> p.getFileName().toString()))
          .noneMatch(name -> name.endsWith(".tmp"));
    }
  }
}

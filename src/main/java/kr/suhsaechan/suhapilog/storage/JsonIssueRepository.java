package kr.suhsaechan.suhapilog.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import kr.suhsaechan.suhapilog.spi.IssueRepository;

/**
 * 이슈 캐시를 JSON 파일로 저장하는 기본 구현.
 *
 * <p>쓰기는 임시 파일에 먼저 기록한 뒤 원자적으로 교체한다. 쓰는 도중 프로세스가
 * 죽어도 기존 파일이 온전히 남는다.
 */
public class JsonIssueRepository implements IssueRepository {

  private static final SuhLogger log = SuhLog.getLogger(JsonIssueRepository.class);

  private static final String ISSUES_FILE = "github-issues.json";
  private static final String HASH_FILE = "github-issues-hash.json";

  private final ObjectMapper objectMapper;
  private final Path storageLocation;
  private final Map<Integer, IssueRef> issueCache = new ConcurrentHashMap<>();
  private final ReentrantLock writeLock = new ReentrantLock();

  private volatile String currentHash = "";

  public JsonIssueRepository(String storagePath) {
    // ObjectMapper는 생성자에서 한 번만 구성한다 — 저장할 때마다 재설정하면
    // 공유 인스턴스를 매번 변형하는 셈이라 스레드 안전성이 깨진다
    this.objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    initializeStorage();
    loadIssues();
    loadHash();
  }

  private void initializeStorage() {
    try {
      Files.createDirectories(this.storageLocation);
    } catch (IOException e) {
      throw new IllegalStateException("저장소 디렉토리를 생성할 수 없습니다: " + this.storageLocation, e);
    }
  }

  private void loadIssues() {
    Path filePath = this.storageLocation.resolve(ISSUES_FILE);
    if (!Files.exists(filePath)) {
      return;
    }
    try {
      String content = Files.readString(filePath, StandardCharsets.UTF_8);
      List<IssueRef> issues = objectMapper.readValue(content, new TypeReference<>() {});
      for (IssueRef issue : issues) {
        if (issue.issueNumber() != null) {
          issueCache.put(issue.issueNumber(), issue);
        }
      }
      log.debug("GitHub 이슈 {}건을 로드했습니다", issues.size());
    } catch (Exception e) {
      // 캐시 파일이 깨져도 라이브러리는 살아 있어야 한다 — 빈 상태로 시작한다
      log.warn("이슈 캐시를 읽지 못해 빈 상태로 시작합니다: {}", e.getMessage());
    }
  }

  private void loadHash() {
    Path filePath = this.storageLocation.resolve(HASH_FILE);
    if (!Files.exists(filePath)) {
      return;
    }
    try {
      String content = Files.readString(filePath, StandardCharsets.UTF_8);
      Map<String, String> hashData =
          objectMapper.readValue(content, new TypeReference<Map<String, String>>() {});
      this.currentHash = hashData.getOrDefault("hash", "");
      log.debug("저장된 해시: {}", this.currentHash);
    } catch (Exception e) {
      log.warn("해시 데이터를 읽지 못했습니다: {}", e.getMessage());
    }
  }

  @Override
  public Optional<IssueRef> findByIssueNumber(int issueNumber) {
    return Optional.ofNullable(issueCache.get(issueNumber));
  }

  @Override
  public IssueRef save(IssueRef issue) {
    issueCache.put(issue.issueNumber(), issue);
    persistIssues();
    return issue;
  }

  @Override
  public Collection<IssueRef> findAll() {
    return new ArrayList<>(issueCache.values());
  }

  @Override
  public void saveAll(Collection<IssueRef> issues) {
    for (IssueRef issue : issues) {
      if (issue != null && issue.issueNumber() != null) {
        issueCache.put(issue.issueNumber(), issue);
      }
    }
    persistIssues(); // 건별이 아니라 한 번만 쓴다
  }

  @Override
  public String getCurrentHash() {
    return this.currentHash;
  }

  @Override
  public void updateHash(String hash) {
    Map<String, String> hashData = new LinkedHashMap<>();
    hashData.put("hash", hash);
    hashData.put("lastUpdated", LocalDateTime.now().toString());
    if (writeJson(HASH_FILE, hashData)) {
      this.currentHash = hash;
      log.debug("해시 갱신: {}", hash);
    }
  }

  private void persistIssues() {
    List<IssueRef> issues = new ArrayList<>(issueCache.values());
    if (writeJson(ISSUES_FILE, issues)) {
      log.debug("GitHub 이슈 {}건을 저장했습니다", issues.size());
    }
  }

  /**
   * 임시 파일에 쓴 뒤 원자적으로 교체한다.
   *
   * @return 성공 여부. 실패해도 예외를 던지지 않는다 — 캐시 저장 실패로 상위 앱을 죽이지 않는다
   */
  private boolean writeJson(String fileName, Object payload) {
    writeLock.lock();
    try {
      Path target = this.storageLocation.resolve(fileName);
      Path tempFile = this.storageLocation.resolve(fileName + ".tmp");

      String json = objectMapper.writeValueAsString(payload);
      Files.writeString(tempFile, json, StandardCharsets.UTF_8);

      try {
        Files.move(tempFile, target,
            StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        // 일부 파일시스템(네트워크 마운트 등)은 원자적 이동을 지원하지 않는다
        Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
      }
      return true;
    } catch (Exception e) {
      log.warn("{} 저장에 실패했습니다: {}", fileName, e.getMessage());
      cleanupTemp(fileName);
      return false;
    } finally {
      writeLock.unlock();
    }
  }

  private void cleanupTemp(String fileName) {
    try {
      Files.deleteIfExists(this.storageLocation.resolve(fileName + ".tmp"));
    } catch (IOException ignored) {
      // 임시 파일 정리 실패는 무시한다 — 다음 쓰기에서 덮어쓴다
    }
  }
}

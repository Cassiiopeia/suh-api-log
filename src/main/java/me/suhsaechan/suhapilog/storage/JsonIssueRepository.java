package me.suhsaechan.suhapilog.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import me.suhsaechan.suhapilog.model.GithubIssue;
import me.suhsaechan.suhapilog.util.SuhApiLogger;

/**
 * GitHub 이슈 정보를 JSON 파일로 저장하고 조회하는 리포지토리
 */
public class JsonIssueRepository implements IssueRepository {
  private static final SuhApiLogger log = SuhApiLogger.getLogger(JsonIssueRepository.class);

  private static final String ISSUES_FILE = "github-issues.json";
  private static final String HASH_FILE = "github-issues-hash.json";

  private final ObjectMapper objectMapper;
  private final Path storageLocation;
  private final Map<Integer, GithubIssue> issueCache = new ConcurrentHashMap<>();
  private String currentHash = "";

  public JsonIssueRepository(String storagePath) {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());

    this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
    initializeStorage();

    loadIssues();
    loadHash();
  }

  /**
   * 저장소 디렉토리가 없다면 생성
   */
  private void initializeStorage() {
    try {
      Files.createDirectories(this.storageLocation);
    } catch (IOException e) {
      throw new RuntimeException("저장소 디렉토리를 생성할 수 없습니다: " + this.storageLocation, e);
    }
  }

  /**
   * 저장된 이슈 정보 로딩
   */
  private void loadIssues() {
    Path filePath = this.storageLocation.resolve(ISSUES_FILE);
    if (Files.exists(filePath)) {
      try {
        String content = Files.readString(filePath);
        List<GithubIssue> issues = objectMapper.readValue(
            content, new TypeReference<List<GithubIssue>>() {});

        issues.forEach(issue -> issueCache.put(issue.getIssueNumber(), issue));
        log.debug("{} GitHub 이슈를 로드했습니다", issues.size());
      } catch (IOException e) {
        log.error("GitHub 이슈를 로드하는 데 실패했습니다: {}", e.getMessage());
      }
    }
  }

  /**
   * 저장된 해시 값 로딩
   */
  private void loadHash() {
    Path filePath = this.storageLocation.resolve(HASH_FILE);
    if (Files.exists(filePath)) {
      try {
        String content = Files.readString(filePath);
        Map<String, String> hashData = objectMapper.readValue(
            content, new TypeReference<Map<String, String>>() {});

        this.currentHash = hashData.getOrDefault("hash", "");
        log.debug("현재 해시 값: {}", this.currentHash);
      } catch (IOException e) {
        log.error("해시 데이터를 로드하는 데 실패했습니다: {}", e.getMessage());
      }
    }
  }

  /**
   * 모든 이슈를 파일에 저장
   */
  private void saveIssues() {
    try {
      List<GithubIssue> issues = new ArrayList<>(issueCache.values());

      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      String json = objectMapper.writeValueAsString(issues);

      Files.writeString(this.storageLocation.resolve(ISSUES_FILE), json);
      log.debug("{} GitHub 이슈를 저장했습니다", issues.size());
    } catch (Exception e) {
      log.error("GitHub 이슈를 저장하는 데 실패했습니다: {}", e.getMessage());
    }
  }

  /**
   * 해시 값 저장
   */
  private void saveHash(String hash) {
    try {
      Map<String, String> hashData = new HashMap<>();
      hashData.put("hash", hash);
      hashData.put("lastUpdated", LocalDateTime.now().toString());

      String json = objectMapper.writeValueAsString(hashData);
      Files.writeString(this.storageLocation.resolve(HASH_FILE), json);
      this.currentHash = hash;
      log.debug("새로운 해시 값 저장: {}", hash);
    } catch (Exception e) {
      log.error("해시 값을 저장하는 데 실패했습니다: {}", e.getMessage());
    }
  }

  @Override
  public Optional<GithubIssue> findByIssueNumber(Integer issueNumber) {
    return Optional.ofNullable(issueCache.get(issueNumber));
  }

  @Override
  public GithubIssue save(GithubIssue issue) {
    issueCache.put(issue.getIssueNumber(), issue);
    saveIssues();
    return issue;
  }

  @Override
  public Collection<GithubIssue> findAll() {
    return new ArrayList<>(issueCache.values());
  }

  @Override
  public void saveAll(Set<Integer> issueNumbers, Collection<GithubIssue> issues) {
    issues.forEach(issue -> issueCache.put(issue.getIssueNumber(), issue));
    saveIssues();

    // 이슈 번호로부터 새 해시 계산 및 저장
    String concatenated = issueNumbers.stream().map(String::valueOf)
        .sorted().collect(Collectors.joining());
    String hash = calculateHash(concatenated);
    saveHash(hash);
  }

  @Override
  public String getCurrentHash() {
    return this.currentHash;
  }

  /**
   * 문자열에서 해시 계산 (MD5)
   */
  private String calculateHash(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (NoSuchAlgorithmException e) {
      log.error("해시 계산에 실패했습니다: {}", e.getMessage());
      throw new RuntimeException("해시 계산을 위한 알고리즘을 찾을 수 없습니다", e);
    }
  }
}
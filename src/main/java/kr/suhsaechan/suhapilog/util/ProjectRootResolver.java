package kr.suhsaechan.suhapilog.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;

/**
 * 프로젝트 루트 탐색, 저장 경로 해석
 * 상대 경로 > 프로젝트 루트 기준 (멀티모듈에서 모듈마다 github-issues 폴더가 생기지 않게)
 */
public final class ProjectRootResolver {

  private static final SuhLogger log = SuhLog.getLogger(ProjectRootResolver.class);

  // 저장 경로 미설정 시 기본 폴더 (프로젝트 루트 기준)
  private static final String DEFAULT_STORAGE_DIRECTORY = "github-issues";

  private ProjectRootResolver() {
  }

  /**
   * working directory부터 상위로 루트 마커 탐색
   * 우선순위 3개: settings.gradle(.kts) > .git > 못 찾으면 working directory
   */
  public static Path resolveProjectRoot() {
    Path workingDirectory = Paths.get("").toAbsolutePath();
    Path searchPath = workingDirectory;
    log.debug("프로젝트 루트 탐색 시작 - working directory: {}", workingDirectory);

    while (searchPath != null) {
      if (hasGradleSettings(searchPath)) {
        log.debug("프로젝트 루트 발견 (settings.gradle): {}", searchPath);
        return searchPath;
      }
      if (Files.exists(searchPath.resolve(".git"))) {
        log.debug("프로젝트 루트 발견 (.git): {}", searchPath);
        return searchPath;
      }
      searchPath = searchPath.getParent();
    }

    log.debug("프로젝트 루트를 찾지 못해 working directory 사용: {}", workingDirectory);
    return workingDirectory;
  }

  /**
   * 설정 경로 > 절대 경로
   * null·빈 값 > 기본 폴더, 절대 경로 > 그대로, 상대 경로 > 프로젝트 루트 기준
   */
  public static Path resolveStoragePath(String configuredPath) {
    String storagePath = (configuredPath == null || configuredPath.isEmpty())
        ? DEFAULT_STORAGE_DIRECTORY
        : configuredPath;

    Path path = Paths.get(storagePath);
    Path resolved = path.isAbsolute()
        ? path.normalize()
        : resolveProjectRoot().resolve(storagePath).normalize();
    log.debug("저장 경로 해석: {} -> {}", configuredPath, resolved);
    return resolved;
  }

  private static boolean hasGradleSettings(Path directory) {
    return Files.exists(directory.resolve("settings.gradle"))
        || Files.exists(directory.resolve("settings.gradle.kts"));
  }
}

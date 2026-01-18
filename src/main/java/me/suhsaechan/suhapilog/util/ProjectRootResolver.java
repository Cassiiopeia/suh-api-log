package me.suhsaechan.suhapilog.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 프로젝트 루트 경로를 탐색하고 저장 경로를 해석하는 유틸리티 클래스.
 * 멀티모듈 프로젝트에서 github-issues 폴더가 여러 모듈에 중복 생성되는 문제를 해결합니다.
 */
public class ProjectRootResolver {

    private ProjectRootResolver() {
        // 유틸리티 클래스
    }

    /**
     * 프로젝트 루트 디렉토리를 탐색합니다.
     * 현재 working directory부터 상위로 올라가며 프로젝트 루트 마커를 찾습니다.
     *
     * <p>탐색 우선순위:</p>
     * <ol>
     *   <li>settings.gradle 또는 settings.gradle.kts 존재 (Gradle 멀티모듈 프로젝트)</li>
     *   <li>.git 디렉토리 존재 (Git 루트)</li>
     *   <li>찾지 못하면 현재 working directory 사용</li>
     * </ol>
     *
     * @return 프로젝트 루트 경로
     */
    public static Path resolveProjectRoot() {
        Path current = Paths.get("").toAbsolutePath();
        Path searchPath = current;

        while (searchPath != null) {
            // Gradle 멀티모듈 프로젝트 (settings.gradle 존재)
            if (Files.exists(searchPath.resolve("settings.gradle")) ||
                Files.exists(searchPath.resolve("settings.gradle.kts"))) {
                return searchPath;
            }
            // Git 루트 (.git 디렉토리 존재)
            if (Files.exists(searchPath.resolve(".git"))) {
                return searchPath;
            }
            searchPath = searchPath.getParent();
        }

        // 찾지 못하면 현재 디렉토리 사용
        return current;
    }

    /**
     * 설정된 저장 경로를 프로젝트 루트 기준 절대 경로로 변환합니다.
     *
     * <p>동작 방식:</p>
     * <ul>
     *   <li>null 또는 빈 문자열: 기본값 "github-issues" 사용</li>
     *   <li>절대 경로: 그대로 사용</li>
     *   <li>상대 경로: 프로젝트 루트 기준으로 변환</li>
     * </ul>
     *
     * @param configuredPath 설정된 경로 (null, 상대 경로, 절대 경로 모두 가능)
     * @return 절대 경로
     */
    public static Path resolveStoragePath(String configuredPath) {
        if (configuredPath == null || configuredPath.isEmpty()) {
            configuredPath = "github-issues";
        }

        Path path = Paths.get(configuredPath);

        // 절대 경로면 그대로 사용
        if (path.isAbsolute()) {
            return path.normalize();
        }

        // 상대 경로면 프로젝트 루트 기준으로 변환
        return resolveProjectRoot().resolve(configuredPath).normalize();
    }
}

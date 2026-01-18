<div align="center">

# ⚡ SUH API Log

**API 변경 이력 문서화, 단 한 줄 어노테이션이면 끝**

<!-- 수정하지마세요 자동으로 동기화 됩니다 -->
## 최신 버전 : v1.2.0 (2025-12-20)

[![Nexus](https://img.shields.io/badge/Nexus-버전_목록-4E9BCD?style=flat-square&logo=sonatype&logoColor=white)](https://nexus.suhsaechan.kr/#browse/browse:maven-releases:me%2Fsuhsaechan%2Fsuh-api-log)
[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE.md)

[빠른 시작](#-빠른-시작) • [주요 기능](#-주요-기능) • [API 문서](#-api-레퍼런스) • [설정](#%EF%B8%8F-설정) • [CHANGELOG](CHANGELOG.md)

</div>

---

## ✨ 왜 SUH-API-LOG인가?

API 변경 이력을 직접 관리하려면 **문서 별도 작성, 버전 관리, 팀원 공유**까지 번거로운 작업이 필요합니다.
이 라이브러리는 모든 복잡함을 숨기고, **어노테이션 한 줄**로 API 변경 이력을 자동 문서화합니다.

| 기존 방식 | 이 라이브러리 |
|----------|--------------|
| 별도 문서에 변경 이력 수동 작성 | ✅ 코드 위에 어노테이션으로 선언 |
| 문서와 코드 동기화 누락 | ✅ 코드와 문서가 항상 일치 |
| GitHub 이슈 링크 수동 관리 | ✅ 이슈 번호만 적으면 자동 연동 |
| Swagger에 별도 설명 추가 | ✅ Swagger UI 자동 통합 |
| 외부 로깅 라이브러리 충돌 | ✅ 순수 POJO, 의존성 독립 |

```java
// 어노테이션 하나면 끝!
@ApiChangeLogs({
    @ApiChangeLog(date = "2024-12-20", author = "서새찬", issueNumber = 42, description = "사용자 조회 API 추가")
})
@GetMapping("/api/users")
public List<User> getUsers() { ... }
```

---

## 🚀 빠른 시작

### 1. 저장소 추가

> ⚠️ **중요**: 이 라이브러리는 Maven Central이 아닌 **SUH Nexus 저장소**에서 제공됩니다.

**Gradle (build.gradle)**
```groovy
repositories {
    mavenCentral()

    // SUH Nexus 저장소 (필수)
    maven {
        name = 'SUH-NEXUS'
        url "https://nexus.suhsaechan.kr/repository/maven-releases/"
        metadataSources {
            mavenPom()
            artifact()
        }
    }
}
```

**Maven (pom.xml)**
```xml
<repositories>
    <repository>
        <id>suh-nexus</id>
        <name>SUH Nexus Repository</name>
        <url>https://nexus.suhsaechan.kr/repository/maven-releases/</url>
    </repository>
</repositories>
```

### 2. 의존성 추가

**Gradle**
```groovy
dependencies {
    implementation 'me.suhsaechan:suh-api-log:X.X.X' // 최신 버전: Nexus에서 확인
}
```

**Maven**
```xml
<dependency>
    <groupId>me.suhsaechan</groupId>
    <artifactId>suh-api-log</artifactId>
    <version>X.X.X</version> <!-- 최신 버전: Nexus에서 확인 -->
</dependency>
```

### 3. 바로 사용

```java
@RestController
public class UserController {

    @ApiChangeLogs(@ApiChangeLog(date = "2024-12-20", author = "서새찬", issueNumber = 42, description = "사용자 목록 조회 API 추가"))
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

**끝!** Spring Boot Auto Configuration으로 별도 설정 없이 바로 사용 가능합니다.

---

## 🎯 주요 기능

### 📝 API 변경 이력 문서화
- 코드 레벨에서 **변경사항 추적**
- 날짜, 작성자, 설명을 구조화된 형태로 관리
- 인터페이스 기반 어노테이션 지원

### 🔗 GitHub 이슈 자동 연동
- 이슈 번호만 적으면 **자동으로 GitHub에서 제목 조회**
- 로컬 JSON 캐싱으로 네트워크 호출 최소화
- Swagger UI에 이슈 링크 자동 삽입

### 📊 Swagger UI 통합
- SpringDoc OpenAPI 자동 감지
- API 문서에 **변경 이력 테이블 자동 생성**
- 별도 설정 없이 바로 동작

### 🎨 순수 POJO 아키텍처
- 외부 로깅 라이브러리 **의존성 완전 제거**
- Java Util Logging 기반 독립 로거
- 기존 프로젝트 로깅 설정에 영향 없음

---

## 📖 API 레퍼런스

### @ApiChangeLog

| 필드 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `date` | String | (필수) | 변경 날짜 (YYYY-MM-DD 형식 권장) |
| `author` | String | (필수) | 작성자 이름 |
| `description` | String | (필수) | 변경 내용 설명 |
| `issueNumber` | int | -1 | GitHub 이슈 번호 (-1이면 링크 미표시) |

### @ApiChangeLogs

| 필드 | 타입 | 설명 |
|------|------|------|
| `value` | ApiChangeLog[] | 여러 변경 이력을 그룹화 |

### 사용 예시

**여러 변경 이력 기록 (한 줄 스타일 - 권장)**
```java
// 👆 최신 변경사항을 위에 배치
@ApiChangeLogs({
    @ApiChangeLog(date = "2024-12-20", author = "김개발", issueNumber = 52, description = "응답 속도 50% 개선"),
    @ApiChangeLog(date = "2024-12-15", author = "이설계", issueNumber = 48, description = "페이징 파라미터 추가"),
    @ApiChangeLog(date = "2024-12-01", author = "서새찬", issueNumber = 42, description = "API 최초 구현")
})
```

**여러 변경 이력 기록 (줄바꿈 스타일)**
```java
@ApiChangeLogs({
    @ApiChangeLog(
        date = "2024-12-20",
        author = "김개발",
        issueNumber = 52,
        description = "응답 속도 50% 개선"
    ),
    @ApiChangeLog(
        date = "2024-12-15",
        author = "이설계",
        issueNumber = 48,
        description = "페이징 파라미터 추가"
    )
})
```

**GitHub 이슈 없이 사용**
```java
@ApiChangeLog(date = "2024-12-20", author = "서새찬", issueNumber = -1, description = "내부 리팩토링")
```

**인터페이스에 어노테이션 적용**
```java
public interface UserService {
    @ApiChangeLogs(@ApiChangeLog(date = "2024-12-15", author = "서새찬", issueNumber = 42, description = "인터페이스 정의"))
    List<User> getAllUsers();
}
```

---

## 🎯 실제 출력 예시

### Swagger UI 변경 이력 테이블

SpringDoc이 포함된 프로젝트에서는 API 문서에 자동으로 변경 이력 테이블이 표시됩니다:

| 날짜 | 작성자 | 이슈 | 변경 내용 |
|------|--------|------|-----------|
| 2024-12-20 | 김개발 | [#52](https://github.com/user/repo/issues/52) | 응답 속도 50% 개선 |
| 2024-12-15 | 이설계 | [#48](https://github.com/user/repo/issues/48) | 페이징 파라미터 추가 |
| 2024-12-01 | 서새찬 | [#42](https://github.com/user/repo/issues/42) | API 최초 구현 |

---

## ⚙️ 설정

`application.yml`에서 세부 설정이 가능합니다:

```yaml
suhapilog:
  enabled: true                    # 기능 활성화 (기본: true)
  github:
    base-url: "https://github.com/username/repository/issues/"  # GitHub 이슈 URL (필수)
  storage:
    path: "./github-issues"        # 이슈 캐시 저장 경로 (기본: ./github-issues)
  default-author: "개발팀"          # 작성자 미지정 시 기본값
```

### 설정 옵션

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `suhapilog.enabled` | boolean | true | 기능 활성화 여부 |
| `suhapilog.github.base-url` | String | null | GitHub 이슈 기본 URL |
| `suhapilog.storage.path` | String | ./github-issues | 캐시 저장 경로 |
| `suhapilog.default-author` | String | "" | 기본 작성자 |

> 💡 **멀티모듈 프로젝트 지원**: 상대 경로(`./github-issues`) 사용 시, 라이브러리가 자동으로 프로젝트 루트(`settings.gradle` 또는 `.git` 위치)를 감지하여 해당 위치에 폴더를 생성합니다. 각 모듈마다 폴더가 중복 생성되지 않습니다.

### 환경별 설정 예시

```yaml
# application-dev.yml (개발 환경)
suhapilog:
  github:
    base-url: "https://github.com/dev-team/project/issues/"
  default-author: "개발팀"

# application-prod.yml (운영 환경)
suhapilog:
  github:
    base-url: "https://github.com/company/production/issues/"
  default-author: "운영팀"
```

---

## 📋 예외 처리

### 발생 가능한 예외

| 예외 | 발생 상황 | 해결 방법 |
|------|----------|----------|
| `IllegalStateException` | GitHub 기능 활성화됐으나 base-url 미설정 | `suhapilog.github.base-url` 설정 추가 |
| `RuntimeException` | 이슈 파싱 실패 | GitHub 이슈 번호 확인 |
| `RuntimeException` | 저장소 디렉토리 생성 실패 | `suhapilog.storage.path` 경로 권한 확인 |

---

## 🔧 요구사항

- **Java**: 17 이상
- **Spring Boot**: 3.x
- **선택사항**: SpringDoc OpenAPI UI 2.x+ (Swagger UI 통합)

---

## 🤔 FAQ

### Q: 기존 로깅 설정에 영향을 주나요?
A: **전혀 없습니다**. suh-api-log는 Java Util Logging 기반의 독립 로거를 사용하여 기존 Logback, Log4j2 등의 설정과 완전히 분리됩니다.

### Q: 성능에 미치는 영향은?
A: 효율적인 JSON 캐싱과 순수 POJO 설계로 성능 영향을 최소화했습니다. GitHub 이슈 조회는 최초 1회만 실행되고 이후 로컬 캐시를 사용합니다.

### Q: Spring Boot 2.x에서 사용 가능한가요?
A: 현재 버전은 Spring Boot 3.x 최적화입니다. 2.x 지원은 향후 계획에 있습니다.

### Q: GitHub 없이도 사용할 수 있나요?
A: 네! `issueNumber = -1`로 설정하면 이슈 링크 없이 변경 이력만 표시됩니다.

### Q: 외부 로깅 라이브러리와 충돌하나요?
A: 전혀 없습니다. 모든 로깅 의존성을 제외하고 Java Util Logging만 사용합니다.

---

## 🤝 기여하기

버그 신고, 기능 요청, 코드 기여를 환영합니다!

- **GitHub**: [https://github.com/Cassiiopeia/suh-api-log](https://github.com/Cassiiopeia/suh-api-log)
- **이슈 트래커**: [https://github.com/Cassiiopeia/suh-api-log/issues](https://github.com/Cassiiopeia/suh-api-log/issues)
- **이메일**: chan4760@gmail.com

---

## 📄 라이선스

MIT License - 자유롭게 사용하세요!

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!**

Made by [SUH-LAB](https://github.com/Cassiiopeia)

</div>

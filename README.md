# suh-api-log ⚡

**suh-api-log**는 Spring Boot API 개발을 위한 **API 변경 이력 문서화 라이브러리**입니다. 코드 레벨에서 API 변경사항을 추적하고 GitHub 이슈와 연동하여 Swagger UI에 자동으로 문서화해주는 전문 도구입니다.

<!-- 수정하지마세요 자동으로 동기화 됩니다 -->
## 최신 버전 : v1.2.1 (2025-12-20)

[전체 버전 기록 보기](CHANGELOG.md)

## ✨ 핵심 특징

### 🚀 제로 설정 시작
- **한 줄 의존성 추가만으로 즉시 사용 가능**
- Spring Boot 자동 설정으로 별도 구성 불필요
- import하는 순간 자동으로 활성화

### 📝 API 변경 이력 문서화 시스템
**주요 기능** (`@ApiChangeLogs`)
- 코드 레벨에서 API 변경사항 추적
- GitHub 이슈 연동으로 상세 컨텍스트 제공
- Swagger UI 자동 통합
- 인터페이스 기반 어노테이션 지원

### 🎨 순수 POJO 아키텍처
**의존성 독립성**
- 외부 로깅 라이브러리 의존성 제거
- Java Util Logging 기반 커스텀 래퍼
- 기존 프로젝트 로깅 설정에 영향 없음

### ⚡ 스마트 기능
- **GitHub 이슈 자동 캐싱**: 네트워크 호출 최소화
- **독립 로거**: 기존 로깅 설정과 완전 분리
- **순수 POJO 설계**: 외부 의존성 최소화
- **성능 최적화**: 효율적인 메모리 사용

---

## 🚀 빠른 시작

### 1. 의존성 추가

#### Gradle
```groovy
repositories {
    mavenCentral()
    // 중요: Suh-Nexus 저장소 필수
    maven {
        url "https://nexus.suhsaechan.kr/repository/maven-releases/"
    }
}

dependencies {
    implementation 'me.suhsaechan:suh-api-log:1.1.4'
}
```

#### Maven
```xml
<dependencies>
    <dependency>
        <groupId>me.suhsaechan</groupId>
        <artifactId>suh-api-log</artifactId>
        <version>1.1.4</version>
    </dependency>
</dependencies>

<!-- 중요: Suh-Nexus 저장소 필수 -->
<repositories>
    <repository>
        <id>suh-project-releases</id>
        <url>https://nexus.suhsaechan.kr/repository/maven-releases/</url>
        <releases><enabled>true</enabled></releases>
        <snapshots><enabled>false</enabled></snapshots>
    </repository>
</repositories>
```

### 2. 즉시 사용 가능! 🎉

의존성을 추가하는 순간부터:
- ✅ API 변경 이력 추적 시스템이 활성화됩니다
- ✅ GitHub 이슈와 자동 연동됩니다
- ✅ Swagger UI에 변경 이력이 표시됩니다

### 3. API 변경 이력 추가

```java
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;

@RestController
public class UserController {

    @ApiChangeLogs({
        @ApiChangeLog(
            date = "2024-12-15",
            author = "서새찬",
            issueNumber = 42,
            description = "사용자 목록 조회 API 추가"
        ),
        @ApiChangeLog(
            date = "2024-12-20",
            author = "홍길동", 
            issueNumber = 45,
            description = "페이징 및 정렬 기능 추가"
        )
    })
    @GetMapping("/api/users")
    public List<User> getUsers() {
        return userService.findAll();
    }
}
```

---

## ⚙️ 설정 옵션

### 기본 설정 (application.yml)

```yaml
# API 변경 이력 시스템 설정
suhapilog:
  enabled: true  # 기본값: true (자동 활성화)
  github:
    base-url: "https://github.com/username/repository/issues/"
  storage:
    path: "./github-issues"  # 이슈 캐시 위치
  default-author: "개발팀"   # 작성자 미지정시 기본값
```

### 고급 설정

#### 선택적 비활성화
```yaml
# API 변경 이력 시스템 비활성화
suhapilog:
  enabled: false
```

#### 개발/운영 환경별 설정
```yaml
# application-dev.yml (개발 환경)
suhapilog:
  github:
    base-url: "https://github.com/dev-team/project/issues/"
  default-author: "개발팀"

# application-prod.yml (운영 환경) 
suhapilog:
  github:
    base-url: "https://github.com/company/production-project/issues/"
  default-author: "운영팀"
```

---

## 📋 상세 사용법

### API 변경 이력 관리

#### 여러 변경 이력 기록
```java
@ApiChangeLogs({
    // 👆 최신 변경사항을 위에 배치 (권장)
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
    ),
    @ApiChangeLog(
        date = "2024-12-01",
        author = "서새찬",
        issueNumber = 42,
        description = "API 최초 구현"
    )
})
```

#### GitHub 이슈 없이 사용
```java
@ApiChangeLogs({
    @ApiChangeLog(
        date = "2024-12-20",
        author = "",  // 비어있으면 default-author 사용
        issueNumber = 0,  // 0이면 이슈 링크 표시 안함
        description = "내부 리팩토링"
    )
})
```

#### 인터페이스에 어노테이션 추가
```java
public interface UserService {
    @ApiChangeLogs({
        @ApiChangeLog(
            date = "2024-12-15",
            author = "서새찬", 
            issueNumber = 42,
            description = "사용자 서비스 인터페이스 정의"
        )
    })
    List<User> getAllUsers();
}

@RestController 
public class UserController implements UserService {
    @GetMapping("/api/users")
    @Override
    public List<User> getAllUsers() {
        // 구현...
    }
}
```

### 커스텀 설정

#### 사용자 정의 설정 클래스
```java
@Configuration
public class ApiChangeLogConfig {
    
    @Bean
    public ApiChangeLogProperties apiChangeLogProperties() {
        ApiChangeLogProperties props = new ApiChangeLogProperties();
        
        // GitHub 설정
        props.getGithub().setBaseUrl("https://github.com/myteam/project/issues/");
        props.setDefaultAuthor("API팀");
        
        // 저장소 설정
        props.getStorage().setPath("./api-changes");
        
        return props;
    }
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

## 🏗️ 아키텍처

```
suh-api-log
├── 📝 API 변경 이력 시스템 (suhapilog)
│   ├── @ApiChangeLogs 어노테이션 처리
│   ├── GitHub 이슈 연동 및 캐싱 
│   ├── Swagger UI 자동 통합
│   ├── 변경 이력 HTML 테이블 생성
│   └── 인터페이스 기반 어노테이션 지원
│
└── 🎨 순수 POJO 로거 (SuhApiLogger)
    ├── Java Util Logging 기반 래퍼
    ├── SLF4J 스타일 API 제공
    ├── 기존 로깅 설정과 격리
    └── 의존성 독립성 보장
```

---

## 🔧 기술 요구사항

- **Java**: 17 이상
- **Spring Boot**: 3.x
- **선택사항**: SpringDoc OpenAPI UI 2.x+ (Swagger UI 통합)

---

## 🤔 FAQ

### Q: 기존 로깅 설정에 영향을 주나요?
A: **전혀 없습니다**. suh-api-log는 독립적인 로거를 사용하여 기존 Logback, Log4j2 등의 설정과 완전히 분리됩니다.

### Q: 성능에 미치는 영향은?
A: 효율적인 캐싱과 순수 POJO 설계로 성능 영향을 최소화했습니다. API 변경 이력 수집은 애플리케이션 시작 시에만 실행됩니다.

### Q: Spring Boot 2.x에서 사용 가능한가요?
A: 현재 버전은 Spring Boot 3.x 최적화입니다. 2.x 지원은 향후 계획에 있습니다.

### Q: GitHub 없이도 사용할 수 있나요?
A: 네! GitHub 설정 없이도 기본적인 API 변경 이력 기능을 사용할 수 있습니다. GitHub 연동은 선택사항입니다.

### Q: 외부 로깅 라이브러리와 충돌하나요?
A: 전혀 없습니다. Java Util Logging 기반의 독립적인 로거를 사용하여 기존 로깅 설정과 완전히 분리됩니다.

---

## 🤝 기여하기

버그 신고, 기능 요청, 코드 기여를 환영합니다!

- **GitHub**: [https://github.com/Cassiiopeia/suh-api-log](https://github.com/Cassiiopeia/suh-api-log)
- **이슈 트래커**: [https://github.com/Cassiiopeia/suh-api-log/issues](https://github.com/Cassiiopeia/suh-api-log/issues)
- **이메일**: chan4760@gmail.com

---

## 📜 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

---

<div align="center">

**⚡ suh-api-log - Spring Boot API 변경 이력 문서화의 새로운 표준 ⚡**

*한번 써보면 없으면 안 되는 그런 도구*

</div>
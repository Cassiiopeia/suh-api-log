# suh-api-log

**suh-api-log**는 Spring Boot 기반 API의 변경 이력을 쉽게 관리하고 Swagger UI에 표시할 수 있는 라이브러리입니다. 이 도구를 사용하면 개발자는 코드 내에서 API 변경 사항을 문서화하고, GitHub 이슈와 연동하여 더 자세한 변경 정보를 제공할 수 있습니다.


## 주요 기능

- **쉬운 통합**: Spring Boot 자동 설정으로 별도 설정 없이 사용 가능
- **@ApiChangeLogs 어노테이션**: API 메서드에 변경 이력을 쉽게 기록
- **GitHub 이슈 통합**: 변경 이력과 GitHub 이슈를 연동하여 더 자세한 컨텍스트 제공
- **Swagger UI 통합**: API 문서에 변경 이력 테이블 자동 표시
- **캐싱 기능**: GitHub 이슈 정보를 로컬에 캐싱하여 성능 최적화
- **설정 커스터마이징**: 다양한 설정으로 원하는 형태로 사용 가능

## 설치 방법

### Gradle

`build.gradle` 파일에 다음 내용을 추가합니다:

```groovy
repositories {
    mavenCentral()
    // 중요: Suh-Nexus 저장소 필수 추가
    maven {
        url "http://suh-project.synology.me:9999/repository/maven-releases/"
        allowInsecureProtocol = true
    }
}

dependencies {
    implementation 'me.suhsaechan:suh-api-log:1.0.0'
}
```

### Maven

`pom.xml` 파일에 다음 내용을 추가합니다:

```xml
<dependencies>
    <dependency>
        <groupId>me.suhsaechan</groupId>
        <artifactId>suh-api-log</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

<!-- 중요: Suh-Nexus 저장소 필수 추가 -->
<repositories>
    <repository>
        <id>suh-project-releases</id>
        <url>http://suh-project.synology.me:9999/repository/maven-releases/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

## 빠른 시작 가이드

### 1. 의존성 추가 및 저장소 설정

위 Gradle 또는 Maven 설정을 프로젝트에 추가합니다. **주의: Suh-Nexus 저장소 설정은 필수입니다.**

### 2. 자동 설정 적용

Spring Boot의 자동 설정 기능을 통해 별도의 설정 없이도 라이브러리가 자동으로 초기화됩니다.

### 3. API 메서드에 변경 이력 어노테이션 추가

```java
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @ApiChangeLogs({
        @ApiChangeLog(
            date = "2024-06-15",
            author = "서새찬",
            issueNumber = 42,
            description = "사용자 조회 API 추가"
        ),
        @ApiChangeLog(
            date = "2024-07-01",
            author = "홍길동",
            issueNumber = 45,
            description = "페이징 기능 추가"
        )
    })
    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getUsers() {
        // API 로직
    }
}
```

### 4. Swagger UI에서 변경 이력 확인

SpringDoc 라이브러리가 포함되어 있다면, API 문서에 자동으로 변경 이력 테이블이 표시됩니다.

## 설정 옵션

`application.yml` 또는 `application.properties`에서 다음과 같이 설정할 수 있습니다:

```yaml
suhapilog:
  # 기능 활성화 여부
  enabled: true
  
  # GitHub 설정
  github:
    base-url: "https://github.com/username/repository/issues/"
  
  # 저장소 설정 (이슈 캐싱 위치)
  storage:
    path: "github-issue"
    
  # 기본 작성자 (author가 비어있을 때 사용)
  default-author: "SUHSAECHAN"
```

## 상세 사용법

### 여러 변경 이력 기록하기

하나의 API에 여러 변경 이력을 기록할 수 있습니다. 가장 최신 변경사항이 목록의 맨 위에 오도록 배치하는 것을 권장합니다.

```java
@ApiChangeLogs({
    @ApiChangeLog(
        date = "2024-07-10",
        author = "서새찬",
        issueNumber = 50,
        description = "성능 최적화"
    ),
    @ApiChangeLog(
        date = "2024-06-20",
        author = "홍길동",
        issueNumber = 45,
        description = "API 응답 형식 변경"
    ),
    @ApiChangeLog(
        date = "2024-06-01",
        author = "서새찬",
        issueNumber = 42,
        description = "API 최초 구현"
    )
})
```

### GitHub 이슈 없이 사용하기

GitHub 이슈 번호를 지정하지 않고도 사용할 수 있습니다:

```java
@ApiChangeLogs({
    @ApiChangeLog(
        date = "2024-07-10",
        author = "서새찬",
        issueNumber = 0,  // 0이면 이슈 링크가 표시되지 않음
        description = "성능 최적화"
    )
})
```

### 인터페이스에 어노테이션 추가

구현 클래스가 아닌 인터페이스에 어노테이션을 추가할 수 있습니다:

```java
public interface UserService {
    @ApiChangeLogs({
        @ApiChangeLog(
            date = "2024-06-15",
            author = "서새찬",
            issueNumber = 42,
            description = "사용자 조회 API 추가"
        )
    })
    List<User> getUsers();
}

@RestController
public class UserController implements UserService {
    @GetMapping("/api/users")
    @Override
    public List<User> getUsers() {
        // API 구현
    }
}
```

## 맞춤 설정 예제

### 설정 파일 통한 커스터마이징

```yaml
suhapilog:
  enabled: false                             # 기능 비활성화
  storage:
    path: "/var/data/api-changelog"          # 저장소 위치 변경
  default-author: "개발팀"                    # 기본 작성자 설정
  github:
    base-url: "https://github.com/my-organization/my-project/issues/"  # GitHub 이슈 URL 변경
```

### 자체 설정 클래스 만들기

필요한 경우 자체 설정 클래스를 작성할 수 있습니다:

```java
@Configuration
@EnableConfigurationProperties
public class MyApiChangeLogConfig {
    @Bean
    @ConfigurationProperties(prefix = "suhapilog")
    public ApiChangeLogProperties apiChangeLogProperties() {
        // 기본 속성에 추가 설정 적용
        ApiChangeLogProperties props = new ApiChangeLogProperties();
        props.setDefaultAuthor("커스텀 작성자");
        return props;
    }
    
    // 다른 빈 커스터마이징...
}
```

## 프로젝트 구조

```
- src/main/java
  - me.suhsaechan.suhapilog
    - annotation
      - ApiChangeLog.java      # 단일 변경 이력 어노테이션
      - ApiChangeLogs.java     # 변경 이력 컨테이너 어노테이션
    - config
      - ApiChangeLogProperties.java       # 설정 속성 클래스
      - SuhApiLogAutoConfiguration.java   # Spring Boot 자동 설정
    - model
      - GithubIssue.java       # GitHub 이슈 모델
    - service
      - ChangelogProcessor.java    # 변경 이력 처리기
      - GithubIssueService.java    # GitHub 이슈 서비스
      - SwaggerIntegration.java    # Swagger 통합
    - storage
      - IssueRepository.java       # 이슈 저장소 인터페이스
      - JsonIssueRepository.java   # JSON 파일 기반 구현
```

## 기술 요구사항

- Java 17 이상
- Spring Boot 3.x
- (선택) SpringDoc OpenAPI UI 2.x 이상 (Swagger UI 통합용)

## 기여하기

버그 신고, 기능 요청, 코드 기여는 GitHub 저장소의 이슈 트래커를 통해 가능합니다:  
[https://github.com/Cassiiopeia/suh-api-log/issues](https://github.com/Cassiiopeia/suh-api-log/issues)

## FAQ

### Q: Spring Boot 2.x에서도 사용할 수 있나요?
A: 현재 버전은 Spring Boot 3.x에 최적화되어 있습니다. Spring Boot 2.x 지원은 향후 계획 중입니다.

### Q: GitHub 이외의 이슈 트래커를 지원하나요?
A: 현재는 GitHub만 지원합니다. 다른 이슈 트래커 지원은 향후 검토할 예정입니다.

### Q: 캐시된 이슈 정보는 어디에 저장되나요?
A: 기본적으로 프로젝트 루트 디렉토리의 github-issue 폴더에 JSON 형식으로 저장됩니다. suhapilog.storage.path 설정으로 변경할 수 있습니다.

## 문의 및 지원

더 자세한 정보나 지원이 필요하면 다음 채널로 문의해주세요:
- GitHub 이슈: [https://github.com/Cassiiopeia/suh-api-log/issues](https://github.com/Cassiiopeia/suh-api-log/issues)
- 이메일: chan4760@gmail.com

## 버전 정보

- **현재 최신 버전**: v1.0.2
- **주요 변경 사항**:

  ### v1.0.2
  - GitHub 이슈 URL 생성 방식 개선
  - 이슈 URL 형식 처리 로직 향상

  ### v1.0.1
  - 버그 수정 및 안정성 개선
  - 문서 업데이트

  ### v1.0.0
  - 초기 릴리스
  - GitHub 이슈 연동 기능
  - Swagger UI 통합
  - Spring Boot 3.x 지원
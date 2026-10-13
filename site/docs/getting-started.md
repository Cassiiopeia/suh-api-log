# 시작하기

세 단계면 됩니다: 저장소 추가 → 의존성 추가 → 어노테이션.

## 1. 저장소와 의존성 추가

두 가지 배포 경로 중 하나를 고르세요. 결과물은 같습니다.

=== "JitPack (권장)"

    별도 계정·서버 없이 GitHub 태그로 바로 받습니다. 버전 앞에 `v`가 붙습니다.

    **Gradle**

    ```groovy
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }

    dependencies {
        implementation 'com.github.Cassiiopeia:suh-api-log:v3.1.0'
        // Swagger UI 통합을 쓴다면 springdoc 을 직접 선언합니다 (아래 표 참고)
        implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.9.1'
    }
    ```

    **Maven**

    ```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

    <dependency>
        <groupId>com.github.Cassiiopeia</groupId>
        <artifactId>suh-api-log</artifactId>
        <version>v3.1.0</version>
    </dependency>
    ```

=== "SUH Nexus"

    메인테이너가 운영하는 Nexus 저장소입니다.

    **Gradle**

    ```groovy
    repositories {
        mavenCentral()
        maven { url 'https://nexus.suhsaechan.kr/repository/maven-releases/' }
    }

    dependencies {
        implementation 'kr.suhsaechan:suh-api-log:3.1.0'
        implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.9.1'
    }
    ```

    **Maven**

    ```xml
    <repositories>
        <repository>
            <id>suh-nexus</id>
            <url>https://nexus.suhsaechan.kr/repository/maven-releases/</url>
        </repository>
    </repositories>

    <dependency>
        <groupId>kr.suhsaechan</groupId>
        <artifactId>suh-api-log</artifactId>
        <version>3.1.0</version>
    </dependency>
    ```

!!! info "Spring Boot 버전별 springdoc"
    | Spring Boot | springdoc-openapi-starter-webmvc-ui |
    |-------------|-------------------------------------|
    | 3.x | 2.x (예: `2.9.1`) |
    | 4.x | 3.x (예: `3.1.1`) |

    SUH API Log 자체는 같은 jar 하나로 두 버전을 모두 지원합니다.

!!! tip "최신 버전"
    최신 버전은 [GitHub Releases](https://github.com/Cassiiopeia/suh-api-log/releases)와
    [JitPack 페이지](https://jitpack.io/#Cassiiopeia/suh-api-log)에서 확인하세요.

## 2. 이슈 연동 켜기 (선택)

이슈 번호로 제목을 가져오려면 이슈 URL 앞부분만 넣습니다. 넣지 않아도 변경 이력 표는 나오고, 이슈 칸만 비어 있습니다.

```yaml title="application.yml"
suhapilog:
  github:
    base-url: "https://github.com/{owner}/{repo}/issues/"
```

## 3. 어노테이션 붙이기

```java
@ApiLog(date = "2026-07-31", author = "서새찬", issueNumber = 17, description = "응답 필드 정리")
@GetMapping("/api/users")
public List<User> getUsers() { ... }
```

앱을 띄우고 `/swagger-ui/index.html`에서 해당 API를 펼치면 변경 이력 표가 보입니다.
Spring Boot 자동 설정이라 `@Enable...` 같은 추가 설정은 없습니다.

![Swagger UI 결과](assets/swagger-changelog.png)

## 직접 돌려 보기

레포의 `examples/demo`가 위 화면을 만든 예제 앱입니다.

```bash
git clone https://github.com/Cassiiopeia/suh-api-log.git
cd suh-api-log
./gradlew -p examples/demo bootRun
# http://localhost:8080/swagger-ui/index.html
```

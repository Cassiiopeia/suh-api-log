예시: 실제 적용 및 사용법
[1] 사용자(=적용 프로젝트)에서의 build.gradle
예를 들면

Gradle
dependencies {
implementation 'me.suhsaechan:suh-api-log:1.0.0'
// ... 기타 프로젝트 의존성
}
이렇게 라이브러리만 추가하면, Spring Boot가 자동설정 클래스를 찾아서 실행합니다.

[2] 사용자 쪽에서 별도 설정이 필요하다면?
(1) properties/yml로 기본 경로 등 오버라이드 가능
YAML
# application.yml
suhapilog:
github:
base-url: "https://github.com/내조직/내레포지토리/issues/"
storage:
path: "./my-github-issue"  # 저장 경로를 바꾸고 싶을 때
위처럼 프로퍼티 바인딩을 지원하도록 @ConfigurationProperties("suhapilog")를 설정 클래스에 달아주면 자동 바인딩!

(2) Bean 오버라이드/수정
특별한 설정이 필요하면 @Configuration을 만들어서 Bean 오버라이드 가능
단, 일반적으로는 라이브러리에서 자동설정으로 충분
[3] 실제 컨트롤러에서 사용 예시
Java
import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    @ApiChangeLogs({
        @ApiChangeLog(
            date = "2025-05-28",
            author = "Cassiiopeia",
            description = "Hello API 최초 구현",
            issueNumber = 100
        ),
        @ApiChangeLog(
            date = "2025-05-29",
            author = "Cassiiopeia",
            description = "테스트 변경 이력 추가",
            issueNumber = 101
        )
    })
    public String hello() {
        return "hello!";
    }
}
[4] Swagger(SpringDoc) 연동
springdoc-openapi를 프로젝트에 같이 넣으면
자동설정에서 OperationCustomizer가 자동으로 붙으면서
Swagger UI에서 description에 변경이력 Table이 표시됨
별도 추가 코드 필요 없음!
[5] 정리 - 사용자 입장 요약
의존성 추가
implementation 'me.suhsaechan:suh-api-log:1.0.0'
(선택) application.yml로 경로, URL 오버라이드
컨트롤러 메서드에 @ApiChangeLogs, @ApiChangeLog 어노테이션 사용
Swagger UI에서는 자동으로 변경 이력 표가 붙음
[6] 예시: 사용법 한눈에 보기

src/main/java/com/example/demo/UserController.java
package com.example.demo;

import me.suhsaechan.suhapilog.annotation.ApiChangeLog;
import me.suhsaechan.suhapilog.annotation.ApiChangeLogs;
import org.springframework.web.bind.annotation.*;

[7] 동작 흐름
사용자는 별다른 설정 없이 어노테이션만 붙이면 끝!
Swagger UI에서 자동으로 API 변경이력 표가 보임.
GitHub 이슈 정보는 지정된 경로(json 파일)로 저장/캐싱됨.
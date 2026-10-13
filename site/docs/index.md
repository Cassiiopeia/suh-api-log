# SUH API Log

**API 변경 이력 문서화, 어노테이션 한 줄이면 끝.**

컨트롤러 메서드에 `@ApiLog`를 붙이면 Swagger UI의 해당 API 설명에 변경 이력 표가 자동으로 생깁니다.
이슈 번호만 적으면 GitHub에서 이슈 제목을 가져와 링크와 함께 보여 줍니다. DB는 필요 없습니다.

```java
@ApiLog(date = "2026-07-31", author = "서새찬", issueNumber = 17, description = "응답 필드 정리 및 로깅 위임")
@ApiLog(date = "2025-05-28", author = "서새찬", issueNumber = 1, description = "사용자 목록 조회 API 최초 구현")
@GetMapping("/api/users")
public List<User> getUsers() { ... }
```

![Swagger UI에 표시된 API 변경 이력 표](assets/swagger-changelog.png)

<small>위 화면은 레포의 `examples/demo` 앱을 실제로 띄워 찍은 것입니다. 이슈 제목은 이 레포의 실제 이슈에서 가져왔습니다.</small>

[시작하기](getting-started.md){ .md-button .md-button--primary }
[GitHub](https://github.com/Cassiiopeia/suh-api-log){ .md-button }

## 왜 쓰나요

| 기존 방식 | SUH API Log |
|----------|-------------|
| 별도 문서에 변경 이력을 손으로 작성 | 코드 위에 어노테이션으로 선언 |
| 문서와 코드가 어긋남 | 코드가 곧 문서 |
| 이슈 링크를 손으로 관리 | 이슈 번호만 적으면 제목·링크 자동 |
| Swagger 설명을 따로 수정 | Swagger UI에 자동 통합 |
| 라이브러리가 로깅 구현체를 끌고 와 충돌 | 로깅·Spring·springdoc 전부 `compileOnly` — 아무것도 강제하지 않음 |

## 이럴 때 맞습니다

- Spring Boot 3.x + springdoc 으로 Swagger UI를 이미 쓰고 있다
- 프론트엔드·QA 팀원이 "이 API 언제 왜 바뀌었지?"를 Swagger에서 바로 보길 원한다
- 변경 이력을 GitHub 이슈와 연결해 관리한다

## 이럴 땐 맞지 않습니다

- **Spring Boot 2.x / Java 8~16** — 지원하지 않습니다 (Java 17+, Spring Boot 3.x).
- **WebFlux 전용 springdoc**(`springdoc-openapi-starter-webflux-ui`) — 현재 WebMVC 기준으로만 검증했습니다.
- **API 버전별 스펙 비교(diff)가 필요할 때** — 이 라이브러리는 사람이 적은 변경 기록을 보여 줄 뿐, OpenAPI 스펙 간 차이를 계산하지 않습니다. 그 용도에는 openapi-diff 같은 도구가 맞습니다.
- **비공개 GitHub 레포의 이슈 제목** — 기본 조회기는 공개 이슈 페이지를 읽습니다. 비공개 레포는 [IssueFetcher를 직접 구현](extending.md)해야 합니다.

## 요구사항

- Java 17 이상
- Spring Boot 3.x
- (선택) springdoc-openapi 2.x — Swagger UI 통합을 쓸 때

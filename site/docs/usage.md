# 사용법

## @ApiLog

| 필드 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `date` | String | (필수) | 변경 날짜 (YYYY-MM-DD 권장 — 정렬 기준) |
| `author` | String | (필수) | 작성자. 빈 문자열이면 `suhapilog.default-author` 사용 |
| `description` | String | (필수) | 변경 내용 |
| `issueNumber` | int | `-1` | 이슈 번호. `-1`이면 이슈 칸을 비움 |

`@ApiLog`는 `@Repeatable`이라 감싸는 어노테이션 없이 여러 번 붙일 수 있습니다.

## 예시

**변경 이력 하나**

```java
@ApiLog(date = "2024-12-20", author = "서새찬", issueNumber = 42, description = "사용자 조회 API 추가")
@GetMapping("/api/users")
public List<User> getUsers() { ... }
```

**여러 개**

```java
@ApiLog(date = "2024-12-20", author = "김개발", issueNumber = 52, description = "응답 속도 개선")
@ApiLog(date = "2024-12-15", author = "이설계", issueNumber = 48, description = "페이징 파라미터 추가")
@ApiLog(date = "2024-12-01", author = "서새찬", issueNumber = 42, description = "API 최초 구현")
@GetMapping("/api/users")
public List<User> getUsers() { ... }
```

**이슈 없이**

```java
@ApiLog(date = "2024-12-20", author = "서새찬", description = "내부 리팩토링")
```

**기본 작성자** — `suhapilog.default-author`를 설정해 두면 `author = ""`로 둔 항목에 그 이름이 들어갑니다.

```yaml
suhapilog:
  default-author: "서새찬"
```

```java
@ApiLog(date = "2024-12-20", author = "", description = "내부 리팩토링")
```

## 동작 순서

1. 부팅 시 `@RestController`·`@Controller` 빈에서 `@ApiLog`의 이슈 번호를 모읍니다.
2. 캐시(기본: JSON 파일)에 없는 이슈만 조회합니다. 기본 방식(`startup-mode: async`)은 백그라운드로 조회해 부팅을 막지 않습니다.
3. springdoc이 OpenAPI 문서를 만들 때 각 API 설명 뒤에 변경 이력 표를 붙입니다.

이슈 조회가 실패해도 예외를 던지지 않습니다. 경고 로그를 남기고 이슈 제목 칸만 비웁니다.

## 예외

| 예외 | 발생 상황 | 해결 |
|------|----------|------|
| `IllegalStateException` | `fail-fast: true`인데 `base-url` 미설정 | `suhapilog.github.base-url` 추가, 또는 `fail-fast: false`(기본값) |
| `IllegalStateException` | 캐시 디렉터리 생성 실패 | `suhapilog.storage.path` 경로 권한 확인 |

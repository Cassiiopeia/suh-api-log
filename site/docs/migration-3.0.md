# 3.0.0 마이그레이션

2.x에서 올라올 때 확인할 것들입니다.

## 반드시 할 일: springdoc 직접 선언

3.0.0부터 springdoc이 `compileOnly`로 바뀌어 라이브러리가 더 이상 끌고 오지 않습니다.
Swagger UI 통합을 쓴다면 직접 선언하세요.

```groovy
dependencies {
    implementation 'kr.suhsaechan:suh-api-log:3.1.0'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.9.1'   // Boot 4 라면 3.1.1
}
```

## 변경 목록

| 변경 | 대응 |
|------|------|
| springdoc → `compileOnly` | 위처럼 직접 선언 |
| `org.reflections` 의존성 제거 | 이 라이브러리를 거쳐 쓰던 곳은 직접 선언 |
| `CommonUtil`·`SuhTimeUtil` 제거 | 대체 없음 (라이브러리와 무관한 코드였음) |
| `config` → `autoconfigure` 패키지 이동 | `ApiChangeLogProperties`를 직접 참조했다면 import 수정 |
| `SuhApiLogger` → `SuhLog` | 당분간 동작(`@Deprecated`). 4.0.0 전까지 이관 |
| `GithubIssue` → `IssueRef` | 당분간 동작(`@Deprecated`). 4.0.0 전까지 이관 |
| `base-url` 미설정 시 부팅 실패 → 경고 | 기존 동작을 원하면 `suhapilog.github.fail-fast: true` |
| 부팅 시 이슈 동기화 차단 → 백그라운드 | 기존 동작을 원하면 `suhapilog.github.startup-mode: blocking` |
| 로깅 exclude 제거, 라이브러리 `application.yml` 제거 | 대응 불필요 — 상위 로깅 설정이 정상 적용됩니다 |

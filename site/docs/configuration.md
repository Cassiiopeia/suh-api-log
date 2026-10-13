# 설정

아무것도 설정하지 않아도 동작합니다. `base-url`만 넣으면 이슈 연동이 켜집니다.
모든 키는 IDE 자동완성(설정 메타데이터)을 지원합니다.

```yaml
suhapilog:
  github:
    base-url: "https://github.com/username/repository/issues/"
```

## 기본

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `suhapilog.enabled` | boolean | `true` | 라이브러리 전체 활성화 |
| `suhapilog.default-author` | String | `""` | `author`가 비었을 때 쓸 작성자 |
| `suhapilog.storage.path` | String | `./github-issues` | 이슈 캐시 저장 경로 |

!!! note "멀티모듈 프로젝트"
    상대 경로를 쓰면 프로젝트 루트(`settings.gradle` 또는 `.git` 위치)를 찾아 그 아래에 폴더를 만듭니다.
    모듈마다 캐시 폴더가 흩어지지 않습니다.

## 로깅

전부 비워 두면 상위 프로젝트의 log4j2/logback 설정을 그대로 따릅니다. 자세한 내용은 [로깅](logging.md).

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `suhapilog.logging.enabled` | boolean | `true` | `false`면 이 라이브러리 로그를 모두 끔 |
| `suhapilog.logging.level` | String | *(없음)* | `trace`/`debug`/`info`/`warn`/`error`. 상위가 허용한 범위를 **좁히기만** 함 |
| `suhapilog.logging.backend` | String | `auto` | `auto`/`slf4j`/`log4j2`/`jul`/`none` |

## GitHub 이슈 조회

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `suhapilog.github.base-url` | String | *(없음)* | 이슈 URL 앞부분. 없으면 이슈 연동만 꺼짐 |
| `suhapilog.github.timeout` | Duration | `5s` | 이슈 페이지 조회 타임아웃 |
| `suhapilog.github.user-agent` | String | `suh-api-log/{version}` | 요청 User-Agent |
| `suhapilog.github.concurrency` | int | `4` | 부팅 시 병렬 조회 스레드 수 |
| `suhapilog.github.startup-mode` | enum | `async` | 아래 표 참고 |
| `suhapilog.github.fail-fast` | boolean | `false` | `true`면 `base-url` 누락 시 부팅 중단 |
| `suhapilog.github.retry.max-attempts` | int | `2` | 최초 시도 포함 총 시도 횟수 |
| `suhapilog.github.retry.backoff` | Duration | `500ms` | 재시도 대기 |
| `suhapilog.github.cache.ttl` | Duration | *(무기한)* | 캐시 유효 기간 |
| `suhapilog.github.cache.negative-ttl` | Duration | `10m` | 조회 실패를 기억하는 시간 |

### startup-mode

| 값 | 동작 | 이럴 때 |
|----|------|--------|
| `async` (기본) | 백그라운드로 미리 조회. 그 사이 요청은 지연 조회로 처리 | 대부분의 경우 |
| `blocking` | 조회가 끝날 때까지 부팅을 막음 (2.x 동작) | 첫 Swagger 화면부터 제목이 꼭 채워져야 할 때 |
| `lazy` | 부팅 때는 아무것도 안 하고 Swagger를 열 때 조회 | 부팅 시간이 가장 중요할 때 |

## 렌더링

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `suhapilog.render.columns` | String | `date,author,issue-number,issue-title,description` | 표시할 컬럼과 순서 |
| `suhapilog.render.sort` | String | `date-desc` | `none`/`date-asc`/`date-desc` |
| `suhapilog.render.title` | String | `API 변경 이력` | 표 위 제목 |
| `suhapilog.render.escape-html` | boolean | `true` | HTML 이스케이프 |

!!! warning "escape-html"
    `false`로 바꾸면 `description`과 이슈 제목이 HTML로 그대로 들어갑니다.
    이슈 제목은 외부(이슈 작성자)가 쓴 텍스트이므로 특별한 이유가 없다면 `true`로 두세요.

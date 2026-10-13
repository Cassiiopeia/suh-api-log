# 확장하기

주요 컴포넌트 4개는 모두 교체할 수 있습니다. 해당 타입의 빈을 하나 등록하면 기본 구현이 물러납니다
(`@ConditionalOnMissingBean`).

| SPI | 기본 구현 | 바꾸는 이유 |
|-----|----------|------------|
| `IssueFetcher` | `JsoupIssueFetcher` (공개 GitHub 이슈 페이지 조회) | GitLab·Jira·사내 트래커, 비공개 레포(API 토큰) |
| `IssueRepository` | `JsonIssueRepository` (JSON 파일) | Redis·DB·인메모리 캐시 |
| `ChangelogRenderer` | `HtmlTableChangelogRenderer` (HTML 표) | 카드형·타임라인형 출력 |
| `TitleNormalizer` | `DefaultTitleNormalizer` | 팀별 이슈 제목 규칙(태그·이모지 제거 등) |

모든 인터페이스는 `kr.suhsaechan.suhapilog.spi` 패키지에 있습니다.

## IssueFetcher — 이슈를 어디서 가져올지

```java
public interface IssueFetcher {
  // 실패하면 Optional.empty(). 예외를 던져 앱을 죽이지 않는다.
  Optional<IssueRef> fetch(int issueNumber);
}
```

예: 사내 트래커 연동

```java
@Bean
public IssueFetcher trackerIssueFetcher(TrackerClient client) {
    return issueNumber -> client.findIssue(issueNumber)
        .map(i -> new IssueRef(issueNumber, i.title(), i.title(), i.url(), LocalDateTime.now()));
}
```

`IssueRef(issueNumber, cleanTitle, rawTitle, pageUrl, fetchedAt)` — `cleanTitle`이 표에 나오는 값이고,
`fetchedAt`은 `cache.ttl` 만료 판단에 쓰입니다.

## IssueRepository — 조회 결과를 어디에 저장할지

```java
public interface IssueRepository {
  Optional<IssueRef> findByIssueNumber(int issueNumber);
  IssueRef save(IssueRef issue);
  Collection<IssueRef> findAll();
  void saveAll(Collection<IssueRef> issues);   // 쓰기를 1회로 묶어야 한다
  String getCurrentHash();                     // 마지막 동기화한 이슈 번호 집합의 해시
  void updateHash(String hash);
}
```

해시는 "이슈 번호 목록이 바뀌었는지"를 판단해 불필요한 재조회를 막는 데 쓰입니다.

## ChangelogRenderer — 어떻게 보여줄지

```java
public interface ChangelogRenderer {
  // entries 가 비면 빈 문자열을 반환해야 한다
  String render(List<ApiLogEntry> entries);
}
```

반환값은 Swagger의 API 설명 뒤에 이어 붙습니다. `ApiLogEntry`에는 작성자 기본값 처리와 이슈 제목 조회가
이미 끝난 값이 들어오므로 렌더러는 출력만 신경 쓰면 됩니다.

```java
@Bean
public ChangelogRenderer bulletRenderer() {
    return entries -> entries.isEmpty() ? "" : entries.stream()
        .map(e -> "- " + e.date() + " " + e.author() + ": " + e.description())
        .collect(Collectors.joining("\n", "\n\n**변경 이력**\n\n", ""));
}
```

!!! warning "HTML 이스케이프"
    직접 렌더러를 만들면 이스케이프도 직접 책임집니다. 이슈 제목은 외부 사용자가 쓴 텍스트입니다.

## TitleNormalizer — 이슈 제목을 어떻게 다듬을지

```java
public interface TitleNormalizer {
  // rawTitle 은 null·빈 문자열일 수 있다. 절대 null 을 반환하지 않는다.
  String normalize(String rawTitle);
}
```

기본 구현은 `🚀 [기능개선][태그]` 같은 접두어를 걷어냅니다. 팀 규칙이 다르면 바꾸세요.

## 기여하기

새 기본 구현(예: GitLab 조회기)을 라이브러리에 넣고 싶다면 [기여 가이드](https://github.com/Cassiiopeia/suh-api-log/blob/main/CONTRIBUTING.md)를 보고
이슈로 먼저 제안해 주세요.

# FAQ

**기존 로깅 설정에 영향을 주나요?**

주지 않습니다. 로깅 구현체를 끌고 오지 않고, 상위 프로젝트의 SLF4J/Log4j2 설정을 그대로 따릅니다.
이 라이브러리 로그만 조정하려면 [로깅](logging.md)을 보세요.

**GitHub 없이도 쓸 수 있나요?**

네. `issueNumber`를 생략하면 이슈 칸 없이 변경 이력만 표시됩니다. `base-url`을 넣지 않으면 이슈 조회 자체를 하지 않습니다.

**비공개 레포 이슈도 되나요?**

기본 조회기는 공개 이슈 페이지를 읽기 때문에 비공개 레포는 제목을 가져오지 못합니다.
GitHub API 토큰을 쓰는 [`IssueFetcher`를 직접 등록](extending.md#issuefetcher)하세요.

**GitLab·Jira를 쓰는데요?**

같은 방법으로 `IssueFetcher`를 구현하면 됩니다. `base-url`은 이슈 링크 주소를 만들고 이슈 연동을 켜는 데 쓰이므로,
해당 트래커의 이슈 URL 앞부분(예: `https://gitlab.com/group/project/-/issues/`)을 넣으세요.

**부팅이 느려지나요?**

기본값(`startup-mode: async`)은 이슈 조회를 백그라운드로 돌려 부팅을 막지 않습니다. 조회 결과는 파일에 캐시되어
다음 부팅부터는 새 이슈 번호만 조회합니다.

**캐시 파일은 커밋해야 하나요?**

커밋하지 않아도 됩니다. 지우면 다음 부팅 때 다시 조회합니다. 보통 `.gitignore`에 `github-issues/`를 넣습니다.

**Swagger 화면에 표가 안 보여요.**

1. springdoc을 의존성으로 직접 선언했는지 확인하세요 (3.0.0부터 필수).
2. 해당 메서드가 `@RestController`/`@Controller` 빈에 있는지 확인하세요.
3. `logging.level.kr.suhsaechan.suhapilog: DEBUG`로 수집 로그를 확인하세요.

그래도 안 되면 [이슈](https://github.com/Cassiiopeia/suh-api-log/issues/new/choose)로 알려 주세요.

# 기여 가이드

SUH API Log에 관심 가져주셔서 고맙습니다. 버그 제보, 기능 제안, 문서 수정, 코드 기여 모두 환영합니다.

## 먼저 이슈부터

- **버그·기능 요청**은 [이슈](https://github.com/Cassiiopeia/suh-api-log/issues/new/choose)로 올려 주세요. 템플릿이 필요한 정보(버전, Spring Boot 버전, 로그)를 묻습니다.
- 큰 변경(새 SPI, 동작 변경)은 PR 전에 이슈로 방향을 맞추면 헛수고를 줄일 수 있습니다.
- 오타·문서 수정처럼 작은 변경은 바로 PR을 올려도 됩니다.
- 보안 문제는 공개 이슈 대신 [SECURITY.md](SECURITY.md)의 경로로 알려 주세요.

## 개발 환경

| 항목 | 버전 |
|------|------|
| JDK | 17 이상 |
| Gradle | 동봉된 Wrapper 사용 (`./gradlew`) |
| Spring Boot | 3.x |

```bash
git clone https://github.com/Cassiiopeia/suh-api-log.git
cd suh-api-log
./gradlew build        # 컴파일 + 테스트
./gradlew test         # 테스트만
```

Nexus 배포 자격증명은 필요 없습니다. 로컬 빌드·테스트·`publishToMavenLocal`은 자격증명 없이 동작합니다.

### 실제 화면으로 확인하기

`examples/demo`는 이 레포의 소스를 그대로 끌어다 쓰는 예제 앱입니다 (`includeBuild`).

```bash
./gradlew -p examples/demo bootRun
# http://localhost:8080/swagger-ui/index.html 에서 GET /api/users 를 펼쳐 보세요
```

## 코드 구조

```
src/main/java/kr/suhsaechan/suhapilog/
├── annotation/     @ApiLog, @ApiLogs
├── autoconfigure/  자동 설정, 설정 프로퍼티(suhapilog.*)
├── core/           컨트롤러 스캔, 이슈 동기화, 변경 이력 조립
├── spi/            교체 가능한 확장점 4종 (IssueFetcher, IssueRepository, ChangelogRenderer, TitleNormalizer)
├── fetch/          기본 IssueFetcher (GitHub 이슈 페이지 조회)
├── storage/        기본 IssueRepository (JSON 파일 캐시)
├── render/         기본 렌더러·제목 정제
├── integration/    springdoc 연동
└── logging/        상위 프로젝트 로깅 위임 (SLF4J → Log4j2 → JUL)
```

새 동작을 넣고 싶다면 대부분 `spi/` 인터페이스의 구현을 추가하는 것으로 충분합니다.
자세한 내용은 [문서 사이트의 확장하기](https://cassiiopeia.github.io/suh-api-log/extending/)를 보세요.

## 지켜 주세요

- **런타임 의존성을 늘리지 않습니다.** Spring·springdoc·로깅은 `compileOnly`입니다. 사용하는 쪽 프로젝트에 무언가를 강제하는 변경은 이슈에서 먼저 논의해 주세요.
- **로깅은 `SuhLog`로만.** 라이브러리 코드에서 SLF4J·Log4j2를 직접 부르지 않습니다.
- **새 동작에는 테스트를 붙입니다.** 기존 테스트는 `src/test/java`에 패키지별로 있습니다.
- **설정 키를 추가하면** `ApiChangeLogProperties`, README의 설정 표, 문서 사이트(`site/docs/configuration.md`)를 함께 고칩니다.

## PR

1. 이 레포를 fork 하고 브랜치를 만듭니다.
2. 변경 후 `./gradlew build`가 통과하는지 확인합니다.
3. PR 템플릿의 변경 사항·테스트 항목을 채워 **`develop`** 으로 올립니다. 관련 이슈가 있으면 본문에 링크해 주세요.
   (`main`은 릴리스 전용입니다. `develop` → `main` 릴리스 PR이 머지되면 버전이 오르고 배포됩니다.)

커밋 메시지 형식은 자유입니다. 메인테이너가 머지할 때 정리합니다.

## 문서 수정

문서 사이트 원본은 `site/docs/`에 있습니다 (MkDocs Material). 로컬 미리보기:

```bash
pip install -r site/requirements.txt
mkdocs serve -f site/mkdocs.yml
```

## 라이선스

기여하신 코드는 이 프로젝트의 [MIT 라이선스](LICENSE.md)로 배포됩니다.

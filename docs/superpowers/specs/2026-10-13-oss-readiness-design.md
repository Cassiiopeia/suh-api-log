# 오픈소스 정비 설계 (#20)

- 이슈: https://github.com/Cassiiopeia/suh-api-log/issues/20
- 근거: 오픈소스 컨설팅 진단(2026-10-13). 레포 주인 목표는 **실사용자 확보**.

## 목표

처음 온 Spring 개발자가 저장소 설정 고민 없이 의존성을 받고, 결과 화면을 보고, 문서 사이트에서 설정·확장법을 찾을 수 있게 한다.

## 결정

| 항목 | 결정 | 이유 |
|------|------|------|
| 배포 경로 | JitPack 추가(권장 경로), Nexus 유지 | 태그가 이미 있어 추가 비용 0. Maven Central은 계정·서명 준비가 필요해 다음 단계로 미룸 |
| 문서 사이트 | MkDocs Material, `site/` 아래 독립 | `docs/`는 설계·산출물 폴더라 섞지 않는다. Java 라이브러리 문서에 흔하고 Node 빌드가 필요 없음 |
| Pages 배포 | Actions(`PROJECT-DOCS-PAGES-DEPLOY.yml`), main push 중 `site/**` 변경 시 | projectops 의 문서 배포 워크플로우 구조를 따름. `--strict` 로 깨진 링크 차단 |
| 결과 화면 | `examples/demo` 를 실제로 띄워 캡처 | 지어낸 목업이 아니라 재현 가능한 화면. 예제 앱은 기여자 확인용으로도 남긴다 |
| GitHub Release | deploy 브랜치 push 시 version.yml 버전 태그로 생성 | Nexus 배포와 같은 시점. 태그는 버전 워크플로우가 이미 만들므로 `--verify-tag` |
| 보안 신고 | GitHub 비공개 취약점 신고 | 이메일 노출 없음 |

## 발견한 버그와 수정

Spring Boot 플러그인이 `jar` 에 `plain` 분류자를 붙여 `-plain.jar` 만 배포되고 있었다(Nexus 2.0.1, JitPack v3.0.1 모두 확인).
pom 이 `packaging=pom` 이 되어 Maven 사용자는 jar 를 받지 못한다. Gradle 은 `.module` 메타데이터로 받아 왔기에 드러나지 않았다.
→ `jar { archiveClassifier = '' }`.

## 범위 밖

- Maven Central 배포
- 영어 README (대상 사용자가 국내 개발자)
- CODE_OF_CONDUCT (외부 기여자 유입 전)

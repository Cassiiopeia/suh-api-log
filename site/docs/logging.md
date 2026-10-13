# 로깅

이 라이브러리는 **로깅 구현체를 하나도 강제하지 않습니다.** SLF4J·Log4j2 API는 `compileOnly`이고,
런타임에 상위 프로젝트에 있는 것을 찾아 씁니다.

탐지 순서: **SLF4J → Log4j2 → JUL(java.util.logging)**

따라서 로그 레벨·출력 형식·파일 저장은 상위 프로젝트의 설정이 그대로 적용됩니다.

## 로그 더 보기

=== "logback (Spring Boot 기본)"

    ```yaml
    logging:
      level:
        kr.suhsaechan.suhapilog: DEBUG
    ```

=== "log4j2"

    ```xml
    <Logger name="kr.suhsaechan.suhapilog" level="debug"/>
    ```

## 라이브러리 로그만 줄이거나 끄기

상위 설정은 건드리지 않고 이 라이브러리만 조정할 수 있습니다.

```yaml
suhapilog:
  logging:
    level: warn      # 상위가 허용한 범위 안에서 warn 이상만
    # enabled: false # 아예 끄기
```

`level`은 범위를 **좁히기만** 합니다. 상위 설정이 막은 DEBUG를 여기서 열 수는 없습니다.

## 백엔드 강제 지정

자동 탐지가 원하는 대로 동작하지 않으면 지정합니다.

```yaml
suhapilog:
  logging:
    backend: log4j2   # auto / slf4j / log4j2 / jul / none
```

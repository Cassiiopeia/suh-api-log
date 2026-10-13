package kr.suhsaechan.suhapilog.integration;

import io.swagger.v3.oas.models.Operation;
import kr.suhsaechan.suhapilog.core.ChangelogService;
import kr.suhsaechan.suhapilog.logging.SuhLog;
import kr.suhsaechan.suhapilog.logging.SuhLogger;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

/**
 * Swagger UI의 오퍼레이션 설명에 변경 이력 표를 덧붙인다.
 *
 * <p>레거시는 springdoc 의존 여부를 몰라 리플렉션 Proxy로 인터페이스를 구현하고
 * 빈 타입을 Object로 선언했다. 그 탓에 springdoc의 타입 기반 빈 수집에서 누락될
 * 위험이 있었다. 이제 &#64;ConditionalOnClass가 클래스패스 존재를 보장하므로
 * 인터페이스를 직접 구현한다.
 */
public class SpringDocOperationCustomizer implements OperationCustomizer {

  private static final SuhLogger log = SuhLog.getLogger(SpringDocOperationCustomizer.class);

  private final ChangelogService changelogService;

  public SpringDocOperationCustomizer(ChangelogService changelogService) {
    this.changelogService = changelogService;
  }

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    if (operation == null || handlerMethod == null) {
      return operation;
    }

    try {
      String table = changelogService.renderFor(handlerMethod.getMethod());
      if (table.isEmpty()) {
        return operation;
      }
      String original = (operation.getDescription() == null) ? "" : operation.getDescription();
      operation.setDescription(original + table);
    } catch (Exception e) {
      // 문서 장식 실패로 Swagger 문서 생성 전체를 깨뜨리지 않는다
      log.warn("변경 이력 렌더링에 실패했습니다 ({}): {}",
          handlerMethod.getMethod().getName(), e.getMessage());
    }

    return operation;
  }
}

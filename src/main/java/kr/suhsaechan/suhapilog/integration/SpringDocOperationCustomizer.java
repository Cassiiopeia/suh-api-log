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
 * <p>&#64;ConditionalOnClass로 springdoc 존재가 보장된 경우에만 등록 > 인터페이스 직접 구현
 * (springdoc 타입 기반 빈 수집 대상)
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

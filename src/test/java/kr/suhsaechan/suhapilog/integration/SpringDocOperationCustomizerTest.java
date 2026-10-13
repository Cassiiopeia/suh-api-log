package kr.suhsaechan.suhapilog.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.Operation;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.suhsaechan.suhapilog.annotation.ApiLog;
import kr.suhsaechan.suhapilog.core.ChangelogService;
import kr.suhsaechan.suhapilog.core.IssueResolver;
import kr.suhsaechan.suhapilog.testsupport.InMemoryIssueRepository;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer;
import kr.suhsaechan.suhapilog.render.HtmlTableChangelogRenderer.Options;
import kr.suhsaechan.suhapilog.spi.IssueFetcher;
import kr.suhsaechan.suhapilog.spi.IssueRef;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

class SpringDocOperationCustomizerTest {

  static class Controller {

    @ApiLog(date = "2026-07-27", author = "서새찬", description = "설명", issueNumber = 17)
    public void documented() {
    }

    public void plain() {
    }
  }

  private SpringDocOperationCustomizer customizer() {
    IssueFetcher fetcher =
        n -> Optional.of(new IssueRef(n, "로깅 위임", "raw", "url", LocalDateTime.now()));
    ChangelogService service = new ChangelogService(
        new IssueResolver(new InMemoryIssueRepository(), fetcher, null),
        new HtmlTableChangelogRenderer(Options.defaults()),
        "https://github.com/o/r/issues/", "");
    return new SpringDocOperationCustomizer(service);
  }

  private HandlerMethod handler(String methodName) throws Exception {
    return new HandlerMethod(new Controller(), Controller.class.getMethod(methodName));
  }

  @Test
  void 어노테이션이_있으면_기존_설명_뒤에_표를_덧붙인다() throws Exception {
    Operation operation = new Operation().description("원래 설명");

    Operation result = customizer().customize(operation, handler("documented"));

    assertThat(result.getDescription()).startsWith("원래 설명");
    assertThat(result.getDescription()).contains("<table>").contains("로깅 위임");
  }

  @Test
  void 설명이_null이어도_동작한다() throws Exception {
    Operation result = customizer().customize(new Operation(), handler("documented"));

    assertThat(result.getDescription()).contains("<table>");
  }

  @Test
  void 어노테이션이_없으면_설명을_건드리지_않는다() throws Exception {
    Operation operation = new Operation().description("원래 설명");

    Operation result = customizer().customize(operation, handler("plain"));

    assertThat(result.getDescription()).isEqualTo("원래 설명");
  }

  @Test
  void null_입력에도_예외를_던지지_않는다() throws Exception {
    assertThat(customizer().customize(null, handler("documented"))).isNull();

    Operation operation = new Operation().description("원래 설명");
    assertThat(customizer().customize(operation, null).getDescription()).isEqualTo("원래 설명");
  }
}

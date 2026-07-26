package kr.suhsaechan.suhapilog.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;

class LoggerBackendResolverTest {

  private ClassLoader base() {
    return LoggerBackendResolverTest.class.getClassLoader();
  }

  @Test
  void slf4j가_바인딩과_함께_있으면_SLF4J를_선택한다() {
    // 테스트 클래스패스에는 spring-boot-starter-test 경유로 slf4j-api + logback이 있다
    assertThat(LoggerBackendResolver.detect(base())).isEqualTo(LoggerBackend.SLF4J);
  }

  @Test
  void slf4j가_없으면_LOG4J2로_폴백한다() {
    // FilteredClassLoader(String...)는 테스트 클래스패스를 부모로 삼고 해당 패키지만 가린다
    try (FilteredClassLoader cl = new FilteredClassLoader("org.slf4j")) {
      assertThat(LoggerBackendResolver.detect(cl)).isEqualTo(LoggerBackend.LOG4J2);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void slf4j도_log4j2도_없으면_JUL로_폴백한다() {
    try (FilteredClassLoader cl =
        new FilteredClassLoader("org.slf4j", "org.apache.logging.log4j")) {
      assertThat(LoggerBackendResolver.detect(cl)).isEqualTo(LoggerBackend.JUL);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void 각_백엔드에_맞는_어댑터를_생성한다() {
    assertThat(LoggerBackendResolver.create(LoggerBackend.SLF4J, "t", base()))
        .isInstanceOf(Slf4jLoggerAdapter.class);
    assertThat(LoggerBackendResolver.create(LoggerBackend.LOG4J2, "t", base()))
        .isInstanceOf(Log4j2LoggerAdapter.class);
    assertThat(LoggerBackendResolver.create(LoggerBackend.JUL, "t", base()))
        .isInstanceOf(JulLoggerAdapter.class);
    assertThat(LoggerBackendResolver.create(LoggerBackend.NONE, "t", base()))
        .isSameAs(NoOpLogger.INSTANCE);
  }

  @Test
  void AUTO를_넘기면_탐지_결과로_해석한다() {
    assertThat(LoggerBackendResolver.create(LoggerBackend.AUTO, "t", base()))
        .isInstanceOf(Slf4jLoggerAdapter.class);
    assertThat(LoggerBackendResolver.create(null, "t", base()))
        .isInstanceOf(Slf4jLoggerAdapter.class);
  }

  @Test
  void 백엔드_문자열을_파싱하고_알_수_없으면_AUTO로_떨어진다() {
    assertThat(LoggerBackend.parseOrAuto("slf4j")).isEqualTo(LoggerBackend.SLF4J);
    assertThat(LoggerBackend.parseOrAuto("LOG4J2")).isEqualTo(LoggerBackend.LOG4J2);
    assertThat(LoggerBackend.parseOrAuto("없는값")).isEqualTo(LoggerBackend.AUTO);
    assertThat(LoggerBackend.parseOrAuto(null)).isEqualTo(LoggerBackend.AUTO);
    assertThat(LoggerBackend.parseOrAuto("  ")).isEqualTo(LoggerBackend.AUTO);
  }
}

package me.suhsaechan.suhapilog.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SuhApiLogger {
  private final Logger log;
  private static final String PREFIX = "[SUH-API-LOG] : ";

  private SuhApiLogger(Class<?> clazz) {
    this.log = Logger.getLogger("SuhLogger." + clazz.getSimpleName());
    configureLogger();
  }

  public static SuhApiLogger getLogger(Class<?> clazz) {
    return new SuhApiLogger(clazz);
  }

  private void configureLogger() {
    // 상위 핸들러 사용하지 않음 (SLF4J와의 연결 차단)
    log.setUseParentHandlers(false);
    
    // 이미 핸들러가 있다면 중복 설정 방지
    if (log.getHandlers().length == 0) {
      // 직접 콘솔 출력 핸들러 추가
      DirectConsoleHandler handler = new DirectConsoleHandler();
      handler.setFormatter(new SuhLogFormatter());
      handler.setLevel(Level.ALL);
      log.addHandler(handler);
    }
    
    log.setLevel(Level.INFO);
  }

  public void debug(String message, Object... args) {
    if (log.isLoggable(Level.FINE)) {
      log.fine(formatMessage(message, args));
    }
  }

  public void info(String message, Object... args) {
    log.info(formatMessage(message, args));
  }

  public void warn(String message, Object... args) {
    log.warning(formatMessage(message, args));
  }

  public void error(String message, Object... args) {
    log.severe(formatMessage(message, args));
  }

  public void error(String message, Throwable t) {
    log.log(Level.SEVERE, formatMessage(message), t);
  }

  private String formatMessage(String message, Object... args) {
    if (args.length == 0) {
      return PREFIX + message;
    }
    return PREFIX + String.format(message.replace("{}", "%s"), args);
  }

  /**
   * SLF4J를 우회하여 직접 콘솔에 출력하는 핸들러
   */
  private static class DirectConsoleHandler extends ConsoleHandler {
    @Override
    public void publish(LogRecord record) {
      if (!isLoggable(record)) {
        return;
      }
      
      String formattedMessage;
      try {
        formattedMessage = getFormatter().format(record);
      } catch (Exception ex) {
        reportError(null, ex, java.util.logging.ErrorManager.FORMAT_FAILURE);
        return;
      }

      try {
        System.out.print(formattedMessage);
        System.out.flush();
      } catch (Exception ex) {
        reportError(null, ex, java.util.logging.ErrorManager.WRITE_FAILURE);
      }
    }
  }

  /**
   * suh-api-log 전용 로그 포맷터
   */
  private static class SuhLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
      StringBuilder sb = new StringBuilder();
      
      // 레벨에 따른 색상 코드 (ANSI)
      String levelColor = getLevelColor(record.getLevel());
      String resetColor = "\u001B[0m";
      
      sb.append(levelColor)
        .append("[")
        .append(record.getLevel().getName())
        .append("]")
        .append(resetColor)
        .append(" ")
        .append(record.getMessage())
        .append(System.lineSeparator());
      
      // 예외가 있다면 스택 트레이스 추가
      if (record.getThrown() != null) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        sb.append(sw.toString());
      }
      
      return sb.toString();
    }
    
    private String getLevelColor(Level level) {
      if (level.intValue() >= Level.SEVERE.intValue()) {
        return "\u001B[31m"; // 빨간색
      } else if (level.intValue() >= Level.WARNING.intValue()) {
        return "\u001B[33m"; // 노란색
      } else if (level.intValue() >= Level.INFO.intValue()) {
        return "\u001B[32m"; // 초록색
      } else {
        return "\u001B[36m"; // 청록색
      }
    }
  }
}

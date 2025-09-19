package me.suhsaechan.suhapilog.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SLF4J 스타일 API를 제공하는 Java Util Logging 래퍼
 * 상위 프로젝트에 로깅 의존성 영향을 주지 않으면서 IntelliJ 스타일 로깅 지원
 */
public class SuhApiLogger {
    private final Logger logger;

    private SuhApiLogger(Logger logger) {
        this.logger = logger;
    }

    public static SuhApiLogger getLogger(Class<?> clazz) {
        return new SuhApiLogger(Logger.getLogger(clazz.getName()));
    }

    public static SuhApiLogger getLogger(String name) {
        return new SuhApiLogger(Logger.getLogger(name));
    }

    // Debug 레벨 로깅
    public void debug(String message) {
        logger.log(Level.FINE, message);
    }

    public void debug(String message, Object arg) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, formatMessage(message, arg));
        }
    }

    public void debug(String message, Object arg1, Object arg2) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, formatMessage(message, arg1, arg2));
        }
    }

    public void debug(String message, Object... args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, formatMessage(message, args));
        }
    }

    // Info 레벨 로깅
    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Object arg) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(formatMessage(message, arg));
        }
    }

    public void info(String message, Object arg1, Object arg2) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(formatMessage(message, arg1, arg2));
        }
    }

    public void info(String message, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(formatMessage(message, args));
        }
    }

    // Warn 레벨 로깅
    public void warn(String message) {
        logger.warning(message);
    }

    public void warn(String message, Object arg) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(formatMessage(message, arg));
        }
    }

    public void warn(String message, Object arg1, Object arg2) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(formatMessage(message, arg1, arg2));
        }
    }

    public void warn(String message, Object... args) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(formatMessage(message, args));
        }
    }

    // Error 레벨 로깅
    public void error(String message) {
        logger.severe(message);
    }

    public void error(String message, Object arg) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(formatMessage(message, arg));
        }
    }

    public void error(String message, Object arg1, Object arg2) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(formatMessage(message, arg1, arg2));
        }
    }

    public void error(String message, Object... args) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(formatMessage(message, args));
        }
    }

    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    // 레벨 체크 메서드
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    /**
     * SLF4J 스타일 메시지 포맷팅 ({} 플레이스홀더 지원)
     */
    private String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        int argIndex = 0;
        int i = 0;

        while (i < message.length()) {
            if (i < message.length() - 1 && message.charAt(i) == '{' && message.charAt(i + 1) == '}') {
                // {} 플레이스홀더 발견
                if (argIndex < args.length) {
                    result.append(String.valueOf(args[argIndex]));
                    argIndex++;
                } else {
                    result.append("{}"); // 인자가 부족하면 그대로 유지
                }
                i += 2; // {} 건너뛰기
            } else {
                result.append(message.charAt(i));
                i++;
            }
        }

        return result.toString();
    }
}
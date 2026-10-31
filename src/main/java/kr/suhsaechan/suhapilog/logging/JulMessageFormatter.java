package kr.suhsaechan.suhapilog.logging;

import java.util.Arrays;

/**
 * SLF4J 스타일 {} 플레이스홀더 포맷터.
 *
 * <p>SLF4J·Log4j2 어댑터는 포맷팅을 백엔드에 위임하므로 이 클래스를 쓰지 않는다.
 * JUL은 {} 문법을 모르기 때문에 폴백 경로에서만 필요하다.
 */
final class JulMessageFormatter {

  private JulMessageFormatter() {
  }

  static String format(String message, Object... args) {
    final String pattern = String.valueOf(message); // null -> "null"
    if (args == null || args.length == 0) {
      return pattern;
    }

    StringBuilder formatted = new StringBuilder(pattern.length() + 16 * args.length);
    int argIndex = 0;
    int i = 0;

    while (i < pattern.length()) {
      // 이스케이프된 플레이스홀더 \{} 는 리터럴 {} 로 남긴다
      if (i <= pattern.length() - 3 && pattern.charAt(i) == '\\' && pattern.charAt(i + 1) == '{'
          && pattern.charAt(i + 2) == '}') {
        formatted.append("{}");
        i += 3;
        continue;
      }
      if (i <= pattern.length() - 2 && pattern.charAt(i) == '{' && pattern.charAt(i + 1) == '}') {
        if (argIndex < args.length) {
          formatted.append(formatArg(args[argIndex++]));
        } else {
          formatted.append("{}"); // 인자가 부족하면 그대로 유지
        }
        i += 2;
      } else {
        formatted.append(pattern.charAt(i));
        i++;
      }
    }

    return formatted.toString();
  }

  private static String formatArg(Object arg) {
    if (arg == null) {
      return "null";
    }
    if (arg.getClass().isArray()) {
      if (arg instanceof Object[] a) return Arrays.deepToString(a);
      if (arg instanceof int[] a) return Arrays.toString(a);
      if (arg instanceof long[] a) return Arrays.toString(a);
      if (arg instanceof double[] a) return Arrays.toString(a);
      if (arg instanceof float[] a) return Arrays.toString(a);
      if (arg instanceof boolean[] a) return Arrays.toString(a);
      if (arg instanceof char[] a) return Arrays.toString(a);
      if (arg instanceof byte[] a) return Arrays.toString(a);
      if (arg instanceof short[] a) return Arrays.toString(a);
    }
    return String.valueOf(arg);
  }
}

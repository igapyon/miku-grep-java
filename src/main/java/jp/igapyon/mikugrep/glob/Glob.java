package jp.igapyon.mikugrep.glob;

import java.util.List;
import java.util.regex.Pattern;

public final class Glob {
    private Glob() {
    }

    public static boolean matchesAny(String value, List<String> patterns) {
        for (String pattern : patterns) {
            if (globMatch(value, pattern)) {
                return true;
            }
        }
        return false;
    }

    public static boolean globMatch(String value, String pattern) {
        return Pattern.compile("^" + escapeGlobPart(pattern) + "$").matcher(value).find();
    }

    public static boolean pathGlobMatch(String value, String pattern) {
        String[] parts = pattern.split("/", -1);
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                escaped.append('/');
            }
            if ("**".equals(parts[index])) {
                escaped.append(".*");
            } else {
                escaped.append(escapeGlobPart(parts[index]));
            }
        }
        return Pattern.compile("^" + escaped + "$").matcher(value).find();
    }

    private static String escapeGlobPart(String pattern) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < pattern.length(); index++) {
            char ch = pattern.charAt(index);
            if (ch == '*') {
                escaped.append("[^/]*");
            } else if (ch == '?') {
                escaped.append("[^/]");
            } else {
                appendRegexEscaped(escaped, ch);
            }
        }
        return escaped.toString();
    }

    private static void appendRegexEscaped(StringBuilder escaped, char ch) {
        switch (ch) {
        case '.':
        case '+':
        case '^':
        case '$':
        case '{':
        case '}':
        case '(':
        case ')':
        case '|':
        case '[':
        case ']':
        case '\\':
            escaped.append('\\');
            escaped.append(ch);
            break;
        default:
            escaped.append(ch);
            break;
        }
    }
}

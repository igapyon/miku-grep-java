package jp.igapyon.mikugrep.regex;

import java.util.ArrayList;
import java.util.List;

public final class RegexSafety {
    private RegexSafety() {
    }

    public static boolean hasNestedQuantifiedGroup(String pattern) {
        List<GroupState> stack = new ArrayList<GroupState>();
        boolean escaped = false;
        boolean inCharClass = false;
        String text = pattern == null ? "" : pattern;

        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (inCharClass) {
                if (ch == ']') {
                    inCharClass = false;
                }
                continue;
            }
            if (ch == '[') {
                inCharClass = true;
                continue;
            }
            if (ch == '(') {
                stack.add(new GroupState());
                continue;
            }
            if (ch == ')') {
                if (stack.isEmpty()) {
                    continue;
                }
                GroupState group = stack.remove(stack.size() - 1);
                char next = index + 1 < text.length() ? text.charAt(index + 1) : '\0';
                boolean groupIsQuantified = next == '*' || next == '+' || next == '?' || next == '{';
                if (group.hasQuantifier && groupIsQuantified) {
                    return true;
                }
                if (group.hasQuantifier && !stack.isEmpty()) {
                    stack.get(stack.size() - 1).hasQuantifier = true;
                }
                continue;
            }
            if ((ch == '*' || ch == '+' || ch == '?' || ch == '{') && !stack.isEmpty()) {
                stack.get(stack.size() - 1).hasQuantifier = true;
            }
        }

        return false;
    }

    private static final class GroupState {
        boolean hasQuantifier;
    }
}

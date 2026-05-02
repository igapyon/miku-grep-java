package jp.igapyon.mikugrep.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegexSafetyTest {
    @ParameterizedTest
    @CsvSource({
            "'^(a+)+$', true",
            "'(.+)+', true",
            "'(a*)+', true",
            "'(a{1,3})*', true",
            "'^([a+])+$', false",
            "'\\(a+\\)+', false",
            "'Repository(Map)?', false"
    })
    void detectsNestedQuantifiedGroups(String pattern, boolean expected) {
        assertEquals(expected, RegexSafety.hasNestedQuantifiedGroup(pattern));
    }
}

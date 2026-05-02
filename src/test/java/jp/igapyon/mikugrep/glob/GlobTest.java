package jp.igapyon.mikugrep.glob;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class GlobTest {
    @Test
    void matchesAnyUsesGlobMatch() {
        assertTrue(Glob.matchesAny("RepositoryMap.java", Arrays.asList("*.txt", "*.java")));
        assertFalse(Glob.matchesAny("RepositoryMap.java", Collections.singletonList("*.md")));
    }

    @Test
    void globMatchKeepsStarAndQuestionInsideSinglePathSegment() {
        assertTrue(Glob.globMatch("RepositoryMap.java", "*.java"));
        assertTrue(Glob.globMatch("App.java", "A??.java"));
        assertFalse(Glob.globMatch("src/App.java", "*.java"));
        assertFalse(Glob.globMatch("App.java", "A?.java"));
    }

    @Test
    void globMatchEscapesRegexCharacters() {
        assertTrue(Glob.globMatch("RepositoryMap.java", "RepositoryMap.java"));
        assertFalse(Glob.globMatch("RepositoryMapXjava", "RepositoryMap.java"));
        assertTrue(Glob.globMatch("file[1].txt", "file[1].txt"));
    }

    @Test
    void pathGlobMatchSupportsDoubleStarPathPart() {
        assertTrue(Glob.pathGlobMatch("src/main/App.java", "src/**/App.java"));
        assertTrue(Glob.pathGlobMatch("src/App.java", "src/*.java"));
        assertFalse(Glob.pathGlobMatch("src/main/App.java", "src/*.java"));
        assertFalse(Glob.pathGlobMatch("src/main/App.java", "lib/**/App.java"));
    }
}

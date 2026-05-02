package jp.igapyon.mikugrep.pathsecurity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class PathSecurityTest {
    @Test
    void acceptsPathsInsideOrEqualToTheBasePath() {
        Path base = Paths.get(File.separator, "tmp", "miku-grep-base").toAbsolutePath().normalize();

        assertTrue(PathSecurity.isPathInsideOrSame(base.toString(), base.toString()));
        assertTrue(PathSecurity.isPathInsideOrSame(base.resolve("src").resolve("file.txt").toString(), base.toString()));
    }

    @Test
    void rejectsSiblingPathsWithTheSamePrefix() {
        Path base = Paths.get(File.separator, "tmp", "miku-grep-base").toAbsolutePath().normalize();
        Path sibling = Paths.get(File.separator, "tmp", "miku-grep-base2", "file.txt").toAbsolutePath().normalize();

        assertFalse(PathSecurity.isPathInsideOrSame(sibling.toString(), base.toString()));
    }

    @Test
    void followsUpstreamPrefixCheckForRelativeDotDotText() {
        Path base = Paths.get(File.separator, "tmp", "miku-grep-base").toAbsolutePath().normalize();

        assertFalse(PathSecurity.isPathInsideOrSame(base.resolve("..local").toString(), base.toString()));
    }
}

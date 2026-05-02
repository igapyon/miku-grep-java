package jp.igapyon.mikugrep.pathsecurity;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathSecurity {
    private PathSecurity() {
    }

    public static boolean isPathInsideOrSame(String candidate, String base) {
        try {
            Path basePath = Paths.get(base).toAbsolutePath().normalize();
            Path candidatePath = Paths.get(candidate).toAbsolutePath().normalize();
            Path relative = basePath.relativize(candidatePath);
            String relativeText = relative.toString();
            if (relativeText.length() == 0) {
                return true;
            }
            return !relativeText.startsWith("..") && !relative.isAbsolute();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}

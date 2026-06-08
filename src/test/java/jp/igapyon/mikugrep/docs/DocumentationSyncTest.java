package jp.igapyon.mikugrep.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikugrep.cli.HelpText;

class DocumentationSyncTest {
    @Test
    void readmeSpecParityAndHelpSharePublicContractPointers() throws Exception {
        String readme = read("README.md");
        String spec = read("docs/miku-grep-cli-spec.md");
        String parity = read("docs/cli-json-parity.md");
        String help = HelpText.helpText();

        assertTrue(readme.contains("docs/miku-grep-cli-spec.md"));
        assertTrue(readme.contains("java -jar target/miku-grep.jar < request.json > result.json"));
        assertTrue(readme.contains("java -jar target/miku-grep.jar TODO . --format json"));
        assertTrue(readme.contains("With query arguments, the default output is human-readable text"));
        assertTrue(readme.contains("Java `Pattern`"));
        assertTrue(readme.contains("Shift_JIS decoding"));

        assertTrue(spec.contains("java -jar target/miku-grep.jar < request.json > result.json"));
        assertTrue(spec.contains("java -jar target/miku-grep.jar QUERY [ROOT]"));
        assertTrue(spec.contains("Args-First Contract"));
        assertTrue(spec.contains("stdout is reserved for result JSON"));
        assertTrue(spec.contains("Unknown request fields are validation errors"));
        assertTrue(spec.contains("Java `Pattern`"));
        assertTrue(spec.contains("Shift_JIS decoding"));

        assertTrue(parity.contains("Documentation Synchronization"));
        assertTrue(parity.contains("docs/miku-grep-cli-spec.md"));
        assertTrue(parity.contains("java -jar target/miku-grep.jar --help"));

        assertTrue(help.contains("docs/miku-grep-cli-spec.md"));
        assertTrue(help.contains("docs/cli-json-parity.md"));
    }

    @Test
    void distributionAssemblyIncludesRuntimeFacingDocuments() throws Exception {
        String assembly = read("src/assembly/dist.xml");

        assertTrue(assembly.contains("<source>${project.build.directory}/${project.build.finalName}.jar</source>"));
        assertTrue(assembly.contains("<destName>${project.artifactId}-${project.version}.jar</destName>"));
        assertTrue(assembly.contains("<source>README.md</source>"));
        assertTrue(assembly.contains("<source>LICENSE</source>"));
        assertTrue(assembly.contains("<source>docs/miku-grep-cli-spec.md</source>"));
        assertTrue(assembly.contains("<source>docs/cli-json-parity.md</source>"));
        assertTrue(assembly.contains("<source>docs/development.md</source>"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}

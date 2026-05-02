package jp.igapyon.mikugrep.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikugrep.model.EffectiveRequest;
import jp.igapyon.mikugrep.model.EncodingOptions;
import jp.igapyon.mikugrep.model.EncodingRuleResult;
import jp.igapyon.mikugrep.model.EncodingRuleType;
import jp.igapyon.mikugrep.model.FileSummaryMatch;
import jp.igapyon.mikugrep.model.FileSummarySnippet;
import jp.igapyon.mikugrep.model.MatchType;
import jp.igapyon.mikugrep.model.MikuGrepRequest;
import jp.igapyon.mikugrep.model.MikuGrepResult;
import jp.igapyon.mikugrep.model.OutputMode;
import jp.igapyon.mikugrep.model.OutputOptions;
import jp.igapyon.mikugrep.model.Query;
import jp.igapyon.mikugrep.model.QueryType;
import jp.igapyon.mikugrep.model.SearchOptions;
import jp.igapyon.mikugrep.model.SearchTarget;
import jp.igapyon.mikugrep.model.Summary;
import jp.igapyon.mikugrep.model.SupportedEncoding;

class MikuGrepJsonTest {
    @Test
    void readsRequestUsingUpstreamJsonFieldNames() throws Exception {
        String json = "{"
                + "\"version\":1,"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + "\"encoding\":{\"default\":\"shift_jis\",\"onDecodeError\":\"skip\"}"
                + "}";

        MikuGrepRequest request = MikuGrepJson.readRequest(json);

        assertEquals(1, request.version);
        assertEquals(".", request.root);
        assertEquals(QueryType.LITERAL, request.query.type);
        assertEquals("RepositoryMap", request.query.text);
        assertEquals(SupportedEncoding.SHIFT_JIS, request.encoding.defaultEncoding);
        assertEquals("skip", request.encoding.onDecodeError);
    }

    @Test
    void keepsUnknownFieldsAvailableForValidationLayer() throws Exception {
        JsonNode tree = MikuGrepJson.readTree("{\"version\":1,\"unknown\":true}");

        assertEquals(true, tree.has("unknown"));
    }

    @Test
    void writesResultWithStableUpstreamFieldOrder() throws Exception {
        MikuGrepResult result = new MikuGrepResult();
        result.version = 1;
        result.ok = true;
        result.error = null;
        result.effectiveRequest = effectiveRequest();
        result.matches = new ArrayList<jp.igapyon.mikugrep.model.MikuGrepMatch>();
        result.matches.add(fileSummaryMatch());
        result.summary = summary();
        result.diagnostics = Collections.emptyList();

        String json = MikuGrepJson.writeResult(result);

        assertEquals("{"
                + "\"version\":1,"
                + "\"ok\":true,"
                + "\"error\":null,"
                + "\"effectiveRequest\":{"
                + "\"root\":\".\","
                + "\"query\":{\"type\":\"literal\",\"text\":\"RepositoryMap\"},"
                + "\"search\":{"
                + "\"target\":\"content\","
                + "\"recursive\":true,"
                + "\"maxDepth\":20,"
                + "\"maxFileBytes\":10485760,"
                + "\"maxLineChars\":1000000,"
                + "\"maxFilesVisited\":100000,"
                + "\"maxDirectoriesVisited\":10000,"
                + "\"includeFileNamePatterns\":[],"
                + "\"excludeFileNamePatterns\":[\"*.class\"],"
                + "\"excludeDirNamePatterns\":[\".git\"]"
                + "},"
                + "\"output\":{"
                + "\"mode\":\"file-summary\","
                + "\"maxMatches\":200,"
                + "\"maxMatchesPerFile\":20,"
                + "\"maxLineLength\":240,"
                + "\"maxSnippetsPerFile\":3"
                + "},"
                + "\"encoding\":{\"default\":\"utf-8\",\"rules\":[],\"onDecodeError\":\"skip\"}"
                + "},"
                + "\"matches\":[{"
                + "\"type\":\"file\","
                + "\"file\":\"README.md\","
                + "\"matchTypes\":[\"content\"],"
                + "\"filenameMatched\":false,"
                + "\"contentMatched\":true,"
                + "\"lines\":[1],"
                + "\"matchCount\":1,"
                + "\"snippets\":[{\"type\":\"content\",\"line\":1,\"text\":\"RepositoryMap\",\"trimmed\":false}],"
                + "\"encoding\":\"utf-8\","
                + "\"encodingRule\":{\"type\":\"default\"}"
                + "}],"
                + "\"summary\":{"
                + "\"filesVisited\":1,"
                + "\"filesScanned\":1,"
                + "\"filesMatched\":1,"
                + "\"matches\":1,"
                + "\"diagnostics\":0,"
                + "\"truncated\":false,"
                + "\"truncatedReason\":null"
                + "},"
                + "\"diagnostics\":[]"
                + "}", json);
    }

    private static EffectiveRequest effectiveRequest() {
        EffectiveRequest request = new EffectiveRequest();
        request.root = ".";
        request.query = new Query();
        request.query.type = QueryType.LITERAL;
        request.query.text = "RepositoryMap";
        request.search = new SearchOptions();
        request.search.target = SearchTarget.CONTENT;
        request.search.recursive = Boolean.TRUE;
        request.search.maxDepth = 20;
        request.search.maxFileBytes = 10485760L;
        request.search.maxLineChars = 1000000;
        request.search.maxFilesVisited = 100000;
        request.search.maxDirectoriesVisited = 10000;
        request.search.includeFileNamePatterns = Collections.emptyList();
        request.search.excludeFileNamePatterns = Collections.singletonList("*.class");
        request.search.excludeDirNamePatterns = Collections.singletonList(".git");
        request.output = new OutputOptions();
        request.output.mode = OutputMode.FILE_SUMMARY;
        request.output.maxMatches = 200;
        request.output.maxMatchesPerFile = 20;
        request.output.maxLineLength = 240;
        request.output.maxSnippetsPerFile = 3;
        request.encoding = new EncodingOptions();
        request.encoding.defaultEncoding = SupportedEncoding.UTF_8;
        request.encoding.rules = Collections.emptyList();
        request.encoding.onDecodeError = "skip";
        return request;
    }

    private static FileSummaryMatch fileSummaryMatch() {
        FileSummarySnippet snippet = new FileSummarySnippet();
        snippet.line = 1;
        snippet.text = "RepositoryMap";
        snippet.trimmed = Boolean.FALSE;

        EncodingRuleResult rule = new EncodingRuleResult();
        rule.type = EncodingRuleType.DEFAULT;

        FileSummaryMatch match = new FileSummaryMatch();
        match.file = "README.md";
        match.matchTypes = Collections.singletonList(MatchType.CONTENT);
        match.filenameMatched = Boolean.FALSE;
        match.contentMatched = Boolean.TRUE;
        match.lines = Arrays.asList(1);
        match.matchCount = 1;
        match.snippets = Collections.singletonList(snippet);
        match.encoding = SupportedEncoding.UTF_8;
        match.encodingRule = rule;
        return match;
    }

    private static Summary summary() {
        Summary summary = new Summary();
        summary.filesVisited = 1;
        summary.filesScanned = 1;
        summary.filesMatched = 1;
        summary.matches = 1;
        summary.diagnostics = 0;
        summary.truncated = false;
        summary.truncatedReason = null;
        return summary;
    }
}

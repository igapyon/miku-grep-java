package jp.igapyon.mikugrep.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.igapyon.mikugrep.model.MikuGrepRequest;
import jp.igapyon.mikugrep.model.MikuGrepResult;

public final class MikuGrepJson {
    private static final ObjectMapper MAPPER = createMapper();

    private MikuGrepJson() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static JsonNode readTree(String json) throws IOException {
        return MAPPER.readTree(json);
    }

    public static JsonNode readTree(InputStream in) throws IOException {
        return MAPPER.readTree(in);
    }

    public static MikuGrepRequest readRequest(String json) throws IOException {
        return MAPPER.readValue(json, MikuGrepRequest.class);
    }

    public static MikuGrepRequest readRequest(InputStream in) throws IOException {
        return MAPPER.readValue(in, MikuGrepRequest.class);
    }

    public static String writeResult(MikuGrepResult result) throws IOException {
        return MAPPER.writeValueAsString(result);
    }

    public static String writePrettyResult(MikuGrepResult result) throws IOException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    public static void writeResult(OutputStream out, MikuGrepResult result) throws IOException {
        MAPPER.writeValue(out, result);
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}

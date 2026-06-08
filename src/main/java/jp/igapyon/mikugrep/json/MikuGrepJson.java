package jp.igapyon.mikugrep.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.igapyon.mikugrep.model.MikuGrepRequest;
import jp.igapyon.mikugrep.model.MikuGrepResult;

public final class MikuGrepJson {
    private static final ObjectMapper MAPPER = createMapper();
    private static final ObjectWriter NODE_PRETTY_WRITER = MAPPER.writer(new NodePrettyPrinter());

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
        return NODE_PRETTY_WRITER.writeValueAsString(result);
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

    private static final class NodePrettyPrinter extends DefaultPrettyPrinter {
        private static final long serialVersionUID = 1L;

        NodePrettyPrinter() {
            indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            indentObjectsWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        }

        NodePrettyPrinter(NodePrettyPrinter base) {
            super(base);
        }

        @Override
        public DefaultPrettyPrinter createInstance() {
            return new NodePrettyPrinter(this);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
            g.writeRaw(": ");
        }

        @Override
        public void writeEndObject(JsonGenerator g, int nrOfEntries) throws IOException {
            if (!_objectIndenter.isInline()) {
                --_nesting;
            }
            if (nrOfEntries > 0) {
                _objectIndenter.writeIndentation(g, _nesting);
            }
            g.writeRaw('}');
        }

        @Override
        public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
            if (!_arrayIndenter.isInline()) {
                --_nesting;
            }
            if (nrOfValues > 0) {
                _arrayIndenter.writeIndentation(g, _nesting);
            }
            g.writeRaw(']');
        }
    }
}

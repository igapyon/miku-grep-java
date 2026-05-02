package jp.igapyon.mikugrep.contract;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public final class RequestFieldShape {
    private final Map<String, RequestFieldShape> fields = new LinkedHashMap<String, RequestFieldShape>();

    public RequestFieldShape field(String name) {
        fields.put(name, null);
        return this;
    }

    public RequestFieldShape object(String name, RequestFieldShape shape) {
        fields.put(name, shape);
        return this;
    }

    public boolean contains(String name) {
        return fields.containsKey(name);
    }

    public RequestFieldShape child(String name) {
        return fields.get(name);
    }

    public String findUnknownField(JsonNode value) {
        return findUnknownField(value, "");
    }

    private String findUnknownField(JsonNode value, String prefix) {
        if (value == null || !value.isObject()) {
            return null;
        }

        Iterator<String> names = value.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!fields.containsKey(name)) {
                return prefix.length() == 0 ? name : prefix + "." + name;
            }
            RequestFieldShape child = fields.get(name);
            if (child != null && !"rules".equals(name)) {
                String childPrefix = prefix.length() == 0 ? name : prefix + "." + name;
                String unknown = child.findUnknownField(value.get(name), childPrefix);
                if (unknown != null) {
                    return unknown;
                }
            }
        }

        return null;
    }
}

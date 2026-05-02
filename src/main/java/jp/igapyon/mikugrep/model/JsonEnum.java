package jp.igapyon.mikugrep.model;

final class JsonEnum {
    private JsonEnum() {
    }

    static <T extends Enum<T> & JsonValueEnum> T fromJsonValue(Class<T> type, String value) {
        for (T item : type.getEnumConstants()) {
            if (item.jsonValue().equals(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("unknown " + type.getSimpleName() + ": " + value);
    }
}

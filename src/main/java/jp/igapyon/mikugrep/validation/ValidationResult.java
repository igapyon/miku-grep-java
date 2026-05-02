package jp.igapyon.mikugrep.validation;

import jp.igapyon.mikugrep.model.EffectiveRequest;

public final class ValidationResult {
    public final boolean ok;
    public final EffectiveRequest effectiveRequest;
    public final String code;
    public final String message;
    public final String path;

    private ValidationResult(boolean ok, EffectiveRequest effectiveRequest, String code, String message, String path) {
        this.ok = ok;
        this.effectiveRequest = effectiveRequest;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public static ValidationResult ok(EffectiveRequest effectiveRequest) {
        return new ValidationResult(true, effectiveRequest, null, null, null);
    }

    public static ValidationResult invalid(String code, String message) {
        return invalid(code, message, null);
    }

    public static ValidationResult invalid(String code, String message, String path) {
        return new ValidationResult(false, null, code, message, path);
    }
}

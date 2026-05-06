package jp.igapyon.mikugrep.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;

import jp.igapyon.mikugrep.coreapi.MikuGrep;
import jp.igapyon.mikugrep.json.MikuGrepJson;
import jp.igapyon.mikugrep.model.MikuGrepResult;

public final class MikuGrepCli {
    public static final String PRODUCT_VERSION = "0.9.0";

    private MikuGrepCli() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        return run(args, System.in, out, err);
    }

    public static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        if (args.length == 1 && "--version".equals(args[0])) {
            out.print("miku-grep " + PRODUCT_VERSION + "\n");
            return 0;
        }
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            out.print(HelpText.helpText());
            return 0;
        }
        if (args.length == 1 && args[0].startsWith("-")) {
            err.print("usage: miku-grep [--version|--help]\n");
            return 2;
        }
        if (args.length > 0) {
            err.print("usage: miku-grep [--version|--help]\n");
            return 2;
        }
        try {
            JsonNode request = MikuGrepJson.readTree(readAll(in));
            MikuGrepResult result = MikuGrep.runRequest(request);
            out.print(MikuGrepJson.writePrettyResult(result));
            out.print("\n");
            return result.ok ? 0 : 1;
        } catch (IOException ex) {
            err.print("malformed stdin: " + ex.getMessage() + "\n");
            return 2;
        } catch (RuntimeException ex) {
            err.print("unexpected runtime error: " + ex.toString() + "\n");
            return 3;
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}

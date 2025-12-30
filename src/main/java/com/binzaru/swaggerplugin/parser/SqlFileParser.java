package com.binzaru.swaggerplugin.parser;

import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlFileParser {

    private static final int HEADER_LINES_TO_SCAN = 20;

    private static final Pattern MODEL_PATTERN = Pattern.compile(
            "--\\s*@model:\\s*(\\w+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SWAGGER_PATTERN = Pattern.compile(
            "--\\s*@swagger:\\s*([^\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    public static String extractModelName(String text) {
        String[] lines = text.split("\\r?\\n");
        int linesToCheck = Math.min(HEADER_LINES_TO_SCAN, lines.length);

        for (int i = 0; i < linesToCheck; i++) {
            Matcher matcher = MODEL_PATTERN.matcher(lines[i]);
            if (matcher.find()) return matcher.group(1);
        }
        return null;
    }

    public static String extractSwaggerPath(String text) {
        String[] lines = text.split("\\r?\\n");
        int linesToCheck = Math.min(HEADER_LINES_TO_SCAN, lines.length);

        for (int i = 0; i < linesToCheck; i++) {
            Matcher matcher = SWAGGER_PATTERN.matcher(lines[i]);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static String[] extractAliasPath(String textBeforeCursor) {
        Pattern pattern = Pattern.compile(
                "\\bAS\\s+[\"']?([\\w.]*)$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(textBeforeCursor);
        if (matcher.find()) {
            String alias = matcher.group(1);

            // Если алиас пустой (AS " или AS )
            if (alias.isEmpty()) {
                return new String[]{""};
            }

            String[] parts = alias.split("\\.");

            // Если заканчивается точкой, добавляем пустую строку
            if (alias.endsWith(".")) {
                String[] result = new String[parts.length + 1];
                System.arraycopy(parts, 0, result, 0, parts.length);
                result[parts.length] = "";
                return result;
            }

            return parts;
        }

        return new String[0];
    }

    public static File resolveSwaggerPath(String swaggerPath, VirtualFile currentFile) {
        if (new File(swaggerPath).isAbsolute()) {
            return new File(swaggerPath);
        } else {
            if (currentFile != null) {
                VirtualFile dir = currentFile.getParent();
                return new File(dir.getPath(), swaggerPath);
            }
            return new File(swaggerPath);
        }
    }
}
package com.binzaru.swaggerplugin.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlFileParser {
    private static final Pattern MODEL_PATTERN = Pattern.compile(
            "--\\s*@model:\\s*(\\w+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SWAGGER_PATTERN = Pattern.compile(
            "--\\s*@swagger:\\s*([^\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    public static String extractModelName(String text) {
        String[] lines = text.split("\n", 20);

        for (String line : lines) {
            Matcher matcher = MODEL_PATTERN.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static String extractSwaggerPath(String text) {
        String[] lines = text.split("\n", 20);

        for (String line : lines) {
            Matcher matcher = SWAGGER_PATTERN.matcher(line);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static String[] extractAliasPath(String textBeforeCursor) {
        Pattern pattern = Pattern.compile(
                "\\bAS\\s+[\"']?([\\w.]+)$",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(textBeforeCursor);
        if (matcher.find()) {
            String alias = matcher.group(1);
            return alias.split("\\.");
        }

        return new String[0];
    }
}
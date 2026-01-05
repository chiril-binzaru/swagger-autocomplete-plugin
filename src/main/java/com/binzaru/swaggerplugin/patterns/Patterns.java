package com.binzaru.swaggerplugin.patterns;

import java.util.regex.Pattern;

public final class Patterns {

    // SQL file annotations
    public static final Pattern MODEL_ANNOTATION = Pattern.compile(
            "--\\s*@model:\\s*(\\w+)",
            Pattern.CASE_INSENSITIVE
    );

    public static final Pattern SWAGGER_ANNOTATION = Pattern.compile(
            "--\\s*@swagger:\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    // Model name autocompletion trigger
    public static final Pattern MODEL_COMPLETION_TRIGGER = Pattern.compile(
            ".*--\\s*@model:\\s*\\w*$",
            Pattern.CASE_INSENSITIVE
    );

    // Alias path extraction after AS
    public static final Pattern ALIAS_PATH = Pattern.compile(
            "\\bAS\\s+[\"']?([\\w.]*)$",
            Pattern.CASE_INSENSITIVE
    );

    private Patterns() {}
}
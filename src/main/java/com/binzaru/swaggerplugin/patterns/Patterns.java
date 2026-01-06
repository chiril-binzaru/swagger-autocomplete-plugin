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

    // Alias path extraction after AS (for autocompletion)
    public static final Pattern ALIAS_PATH = Pattern.compile(
            "\\bAS\\s+[\"']?([\\w.]*)$",
            Pattern.CASE_INSENSITIVE
    );

    // Full alias for validation (captures complete alias after AS, with optional quotes)
    // Group 1: opening quote (optional), Group 2: alias path
    public static final Pattern ALIAS_VALIDATION = Pattern.compile(
            "\\bAS\\s+([\"']?)([\\w.]+)[\"']?",
            Pattern.CASE_INSENSITIVE
    );

    // For toggling quotes: Group 1: whitespace, Group 2: opening quote, Group 3: alias, Group 4: closing quote
    public static final Pattern ALIAS_WITH_QUOTES = Pattern.compile(
            "\\bAS(\\s+)([\"']?)([\\w.]+)([\"']?)",
            Pattern.CASE_INSENSITIVE
    );

    private Patterns() {}
}
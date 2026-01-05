package com.binzaru.swaggerplugin.patterns;

import java.util.regex.Pattern;

public final class Patterns {

    // Аннотации в SQL файлах
    public static final Pattern MODEL_ANNOTATION = Pattern.compile(
            "--\\s*@model:\\s*(\\w+)",
            Pattern.CASE_INSENSITIVE
    );

    public static final Pattern SWAGGER_ANNOTATION = Pattern.compile(
            "--\\s*@swagger:\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    // Триггер автодополнения для имени модели
    public static final Pattern MODEL_COMPLETION_TRIGGER = Pattern.compile(
            ".*--\\s*@model:\\s*\\w*$",
            Pattern.CASE_INSENSITIVE
    );

    // Извлечение пути алиаса после AS
    public static final Pattern ALIAS_PATH = Pattern.compile(
            "\\bAS\\s+[\"']?([\\w.]*)$",
            Pattern.CASE_INSENSITIVE
    );

    private Patterns() {}
}
package com.binzaru.swaggerplugin.patterns;

import java.util.regex.Pattern;

/**
 * Contains all regular expression patterns used throughout the plugin
 * for parsing SQL files with Swagger annotations.
 */
public final class Patterns {

    /**
     * Matches the @model annotation in SQL comments and captures the model name.
     * <p>
     * Example matches: {@code -- @model: CustomerProfile}, {@code --@model:Order}
     * <p>
     * Pattern breakdown:
     * <pre>
     * --        Literal SQL comment start
     * \s*       Zero or more whitespace characters (optional space after --)
     * @model:   Literal annotation name
     * \s*       Zero or more whitespace characters (optional space after colon)
     * (\w+)     GROUP 1: One or more word characters [a-zA-Z0-9_] - the model name
     * </pre>
     * Flag: {@code CASE_INSENSITIVE} - matches @model, @Model, @MODEL, etc.
     */
    public static final Pattern MODEL_ANNOTATION = Pattern.compile(
            "--\\s*@model:\\s*(\\w+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches the @swagger annotation in SQL comments and captures the file path.
     * <p>
     * Example matches: {@code -- @swagger: ./schema.json}, {@code -- @swagger: D:\path\to\swagger.json}
     * <p>
     * Pattern breakdown:
     * <pre>
     * --        Literal SQL comment start
     * \s*       Zero or more whitespace characters
     * @swagger: Literal annotation name
     * \s*       Zero or more whitespace characters
     * (\S+)     GROUP 1: One or more non-whitespace characters - the file path
     * </pre>
     * Flag: {@code CASE_INSENSITIVE}
     */
    public static final Pattern SWAGGER_ANNOTATION = Pattern.compile(
            "--\\s*@swagger:\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Detects if the cursor is positioned after @model: annotation for triggering autocompletion.
     * <p>
     * Example matches: {@code -- @model: }, {@code -- @model: Cust}
     * <p>
     * Pattern breakdown:
     * <pre>
     * .*        Any characters (greedy) - content before the annotation
     * --        Literal SQL comment start
     * \s*       Zero or more whitespace characters
     * @model:   Literal annotation name
     * \s*       Zero or more whitespace characters
     * \w*       Zero or more word characters - partial model name being typed
     * $         End of string (cursor position)
     * </pre>
     * Flag: {@code CASE_INSENSITIVE}
     */
    public static final Pattern MODEL_COMPLETION_TRIGGER = Pattern.compile(
            ".*--\\s*@model:\\s*\\w*$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Extracts the alias path after AS keyword for autocompletion.
     * Captures partial input including trailing dots for nested field navigation.
     * <p>
     * Example matches: {@code AS addr}, {@code AS address.}, {@code AS "billing.street}
     * <p>
     * Pattern breakdown:
     * <pre>
     * \b        Word boundary (ensures AS is a separate word, not part of "ALIAS")
     * AS        Literal AS keyword
     * \s+       One or more whitespace characters
     * ["']?     Optional opening quote (double or single)
     * ([\w.]*) GROUP 1: Zero or more word characters or dots - the alias path
     * $         End of string (cursor position)
     * </pre>
     * Flag: {@code CASE_INSENSITIVE}
     */
    public static final Pattern ALIAS_PATH = Pattern.compile(
            "\\bAS\\s+[\"']?([\\w.]*)$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches complete aliases after AS keyword for validation purposes.
     * Captures both the optional quote and the alias path separately.
     * <p>
     * Example matches: {@code AS customerId}, {@code AS "address.street"}, {@code AS 'field'}
     * <p>
     * Pattern breakdown:
     * <pre>
     * \b        Word boundary
     * AS        Literal AS keyword
     * \s+       One or more whitespace characters
     * (["']?)   GROUP 1: Optional opening quote (captured to detect quoted aliases)
     * ([\w.]+)  GROUP 2: One or more word characters or dots - the alias path
     * ["']?     Optional closing quote (not captured)
     * </pre>
     * Flag: {@code CASE_INSENSITIVE}
     */
    public static final Pattern ALIAS_VALIDATION = Pattern.compile(
            "\\bAS\\s+([\"']?)([\\w.]+)[\"']?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Matches aliases with full quote information for the toggle quotes action.
     * Captures whitespace, quotes, and alias separately for precise replacement.
     * <p>
     * Example matches: {@code AS fieldName}, {@code AS  "quotedField"}, {@code AS 'singleQuoted'}
     * <p>
     * Pattern breakdown:
     * <pre>
     * \b        Word boundary
     * AS        Literal AS keyword
     * (\s+)     GROUP 1: One or more whitespace characters (preserved during replacement)
     * (["']?)   GROUP 2: Optional opening quote
     * ([\w.]+)  GROUP 3: One or more word characters or dots - the alias path
     * (["']?)   GROUP 4: Optional closing quote
     * </pre>
     * Flag: {@code CASE_INSENSITIVE}
     */
    public static final Pattern ALIAS_WITH_QUOTES = Pattern.compile(
            "\\bAS(\\s+)([\"']?)([\\w.]+)([\"']?)",
            Pattern.CASE_INSENSITIVE
    );

    private Patterns() {}
}
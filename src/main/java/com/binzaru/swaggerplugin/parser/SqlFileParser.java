package com.binzaru.swaggerplugin.parser;

import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;

import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class for parsing SQL files with Swagger annotations.
 * <p>
 * Extracts metadata from SQL file headers including @swagger and @model annotations,
 * and parses alias paths for autocompletion.
 */
public class SqlFileParser {

    /** Number of lines to scan from the beginning of the file when looking for annotations. */
    private static final int HEADER_LINES_TO_SCAN = 20;

    /**
     * Extracts the model name from a @model annotation in the SQL file header.
     * <p>
     * Scans the first {@value #HEADER_LINES_TO_SCAN} lines of the file for a line
     * matching the pattern {@code -- @model: ModelName}.
     *
     * @param text the full text content of the SQL file
     * @return the model name if found, or {@code null} if no @model annotation exists
     */
    public static String extractModelName(String text) {
        String[] lines = text.split("\\r?\\n");
        int linesToCheck = Math.min(HEADER_LINES_TO_SCAN, lines.length);

        for (int i = 0; i < linesToCheck; i++) {
            Matcher matcher = Patterns.MODEL_ANNOTATION.matcher(lines[i]);
            if (matcher.find()) return matcher.group(1);
        }
        return null;
    }

    /**
     * Extracts the Swagger file path from a @swagger annotation in the SQL file header.
     * <p>
     * Scans the first {@value #HEADER_LINES_TO_SCAN} lines of the file for a line
     * matching the pattern {@code -- @swagger: path/to/swagger.json}.
     * The path can be either absolute or relative to the SQL file location.
     *
     * @param text the full text content of the SQL file
     * @return the Swagger file path if found, or {@code null} if no @swagger annotation exists
     */
    public static String extractSwaggerPath(String text) {
        String[] lines = text.split("\\r?\\n");
        int linesToCheck = Math.min(HEADER_LINES_TO_SCAN, lines.length);

        for (int i = 0; i < linesToCheck; i++) {
            Matcher matcher = Patterns.SWAGGER_ANNOTATION.matcher(lines[i]);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    /**
     * Extracts and parses the alias path from text preceding the cursor.
     * <p>
     * Parses alias paths like {@code AS address.street} into path segments for navigation
     * through nested Swagger models. The last segment is used as a prefix for filtering
     * autocompletion suggestions.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code "AS "} → {@code [""]}</li>
     *   <li>{@code "AS addr"} → {@code ["addr"]}</li>
     *   <li>{@code "AS address."} → {@code ["address", ""]}</li>
     *   <li>{@code "AS address.str"} → {@code ["address", "str"]}</li>
     * </ul>
     *
     * @param textBeforeCursor the text from the start of the current line to the cursor position
     * @return an array of path segments; empty array if no AS keyword found;
     *         trailing empty string indicates the alias ends with a dot
     */
    public static String[] extractAliasPath(String textBeforeCursor) {
        Matcher matcher = Patterns.ALIAS_PATH.matcher(textBeforeCursor);
        if (matcher.find()) {
            String alias = matcher.group(1);

            // If alias is empty (AS " or AS )
            if (alias.isEmpty()) {
                return new String[]{""};
            }

            String[] parts = alias.split("\\.");

            // If ends with a dot, add an empty string
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

    /**
     * Resolves a Swagger file path to an absolute File object.
     * <p>
     * If the provided path is absolute, it is returned as-is.
     * If the path is relative, it is resolved against the parent directory of the current file.
     *
     * @param swaggerPath the Swagger file path extracted from @swagger annotation via {@link #extractSwaggerPath(String)} (can be absolute or relative)
     * @param currentFile the current .sql file as VirtualFile (required; used to resolve relative paths)
     * @return a File object representing the absolute path to the Swagger file
     * @throws NullPointerException if currentFile is null
     */
    public static File resolveSwaggerPath(String swaggerPath, @NotNull VirtualFile currentFile) {
        File file;
        if (!new File(swaggerPath).isAbsolute()) {
            VirtualFile dir = currentFile.getParent();
            file = new File(dir.getPath(), swaggerPath);
        } else {
            file = new File(swaggerPath);
        }

        return file.toPath().normalize().toFile();
    }
}
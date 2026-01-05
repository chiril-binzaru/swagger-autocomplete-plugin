package com.binzaru.swaggerplugin.parser;

import com.binzaru.swaggerplugin.patterns.Patterns;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;

import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;

public class SqlFileParser {

    private static final int HEADER_LINES_TO_SCAN = 20;

    public static String extractModelName(String text) {
        String[] lines = text.split("\\r?\\n");
        int linesToCheck = Math.min(HEADER_LINES_TO_SCAN, lines.length);

        for (int i = 0; i < linesToCheck; i++) {
            Matcher matcher = Patterns.MODEL_ANNOTATION.matcher(lines[i]);
            if (matcher.find()) return matcher.group(1);
        }
        return null;
    }

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

    public static String[] extractAliasPath(String textBeforeCursor) {
        Matcher matcher = Patterns.ALIAS_PATH.matcher(textBeforeCursor);
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
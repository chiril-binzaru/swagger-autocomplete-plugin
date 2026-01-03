package com.binzaru.swaggerplugin.validation;

import com.binzaru.swaggerplugin.parser.SqlFileParser;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwaggerPathValidator implements Annotator {
    private static final Pattern SWAGGER_PATTERN = Pattern.compile(
            "--\\s*@swagger:\\s*([^\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        // Обрабатываем только листовые элементы (без детей), чтобы избежать двойного срабатывания
        if (element.getFirstChild() != null) {
            return;
        }

        String text = element.getText();

        // Обрабатываем только если весь текст элемента содержит @swagger
        if (!text.trim().startsWith("--") || !text.contains("@swagger")) {
            return;
        }

        Matcher matcher = SWAGGER_PATTERN.matcher(text);

        if (matcher.find()) {
            String path = matcher.group(1);
            int start = element.getTextRange().getStartOffset() + matcher.start(1);
            int end = element.getTextRange().getStartOffset() + matcher.end(1);
            TextRange range = new TextRange(start, end);

            // Резолвим путь относительно текущего файла
            VirtualFile currentFile = element.getContainingFile().getVirtualFile();
            File swaggerFile = SqlFileParser.resolveSwaggerPath(path, currentFile);

            if (swaggerFile.exists() && swaggerFile.isFile()) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.STRING)
                        .create();
            } else {
                holder.newAnnotation(HighlightSeverity.ERROR, "Swagger file not found: " + path)
                        .range(range)
                        .create();
            }
        }
    }
}
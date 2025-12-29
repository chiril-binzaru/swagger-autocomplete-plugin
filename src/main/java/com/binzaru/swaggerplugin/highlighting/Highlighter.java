package com.binzaru.swaggerplugin.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlighter implements Annotator {
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile(
            "--\\s*(@model:|@swagger:)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PARAMETER_PATTERN = Pattern.compile(
            "&(\\w+)",  // &param_name
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getFirstChild() != null) {
            return;
        }

        String text = element.getText();

        // Подсвечиваем аннотации
        Matcher annotationMatcher = ANNOTATION_PATTERN.matcher(text);
        while (annotationMatcher.find()) {
            int start = element.getTextRange().getStartOffset() + annotationMatcher.start(1);
            int end = element.getTextRange().getStartOffset() + annotationMatcher.end(1);

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(new TextRange(start, end))
                    .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                    .create();
        }

        // Подсвечиваем параметры
        Matcher parameterMatcher = PARAMETER_PATTERN.matcher(text);
        while (parameterMatcher.find()) {
            int start = element.getTextRange().getStartOffset() + parameterMatcher.start();
            int end = element.getTextRange().getStartOffset() + parameterMatcher.end();

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(new TextRange(start, end))
                    .textAttributes(DefaultLanguageHighlighterColors.STATIC_METHOD)
                    .create();
        }
    }
}
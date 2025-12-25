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

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        String text = element.getText();

        Matcher matcher = ANNOTATION_PATTERN.matcher(text);
        while (matcher.find()) {
            int start = element.getTextRange().getStartOffset() + matcher.start(1);
            int end = element.getTextRange().getStartOffset() + matcher.end(1);

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(new TextRange(start, end))
                    .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                    .create();
        }
    }
}